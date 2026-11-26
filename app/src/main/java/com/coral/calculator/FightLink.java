package com.coral.calculator;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The line between two devices playing Page brawl against each other. One of them hosts: it
 * waits for the other, runs the fight, and sends how the fight stands many times a second.
 * The other joins: it sends what its player holds down and shows what it is sent. They talk
 * straight to each other over the network both are on; nothing goes anywhere else.
 *
 * A message is a kind, a length and that many bytes. Everything is read and written on
 * threads of its own; whoever uses the line asks for what has arrived with {@link #poll}.
 * How the fight stands is only worth sending while it is the latest, so a newer one takes
 * the place of an older one that has not gone yet.
 */
final class FightLink {
    static final int PORT = 47816, VERSION = 1;
    /** The kinds of message: who I am, what I hold down, how the fight stands, a fight beginning, a wish for another, and leaving. */
    static final int HELLO = 1, PAD = 2, STATE = 3, START = 4, AGAIN = 5, BYE = 6;
    private static final int LONGEST = 4096;

    private final ConcurrentLinkedQueue<byte[]> arrived = new ConcurrentLinkedQueue<>();
    private final LinkedBlockingQueue<byte[]> going = new LinkedBlockingQueue<>();
    private final AtomicReference<byte[]> standing = new AtomicReference<>();
    private volatile ServerSocket server;
    private volatile Socket socket;
    private volatile boolean connected, closed;
    private volatile String problem;

    private FightLink() {}

    /** Waits on a port for the other device to join. */
    static FightLink host(int port) {
        FightLink link = new FightLink();
        Thread waiting = new Thread(() -> {
            try {
                ServerSocket server = new ServerSocket();
                server.setReuseAddress(true);
                server.bind(new InetSocketAddress(port));
                link.server = server;
                if (link.closed) {
                    server.close();
                    return;
                }
                link.talk(server.accept());
            } catch (IOException failed) {
                link.fail(failed);
            }
        }, "brawl-host");
        waiting.setDaemon(true);
        waiting.start();
        return link;
    }

    /** Joins a device that is waiting at an address. */
    static FightLink join(String address, int port) {
        FightLink link = new FightLink();
        Thread joining = new Thread(() -> {
            try {
                Socket socket = new Socket();
                socket.connect(new InetSocketAddress(address, port), 6000);
                link.talk(socket);
            } catch (IOException failed) {
                link.fail(failed);
            }
        }, "brawl-join");
        joining.setDaemon(true);
        joining.start();
        return link;
    }

    private void fail(IOException failed) {
        if (!closed) problem = failed.getMessage() == null ? failed.getClass().getSimpleName() : failed.getMessage();
        closed = true;
        connected = false;
    }

    /** Reads on this thread and writes on another, until the line is closed from either end. */
    private void talk(Socket joined) throws IOException {
        socket = joined;
        joined.setTcpNoDelay(true);
        if (closed) {
            joined.close();
            return;
        }
        connected = true;
        DataOutputStream out = new DataOutputStream(joined.getOutputStream());
        Thread writing = new Thread(() -> {
            try {
                while (!closed) {
                    byte[] message = going.poll(4, TimeUnit.MILLISECONDS);
                    if (message == null) message = standing.getAndSet(null);
                    if (message == null) continue;
                    out.writeByte(message[0]);
                    out.writeShort(message.length - 1);
                    out.write(message, 1, message.length - 1);
                    out.flush();
                }
            } catch (IOException | InterruptedException failed) {
                closed = true;
                connected = false;
            }
        }, "brawl-write");
        writing.setDaemon(true);
        writing.start();
        DataInputStream in = new DataInputStream(joined.getInputStream());
        try {
            while (!closed) {
                int kind = in.readUnsignedByte(), length = in.readUnsignedShort();
                if (length > LONGEST) throw new IOException("too long a message");
                byte[] message = new byte[length + 1];
                message[0] = (byte) kind;
                in.readFully(message, 1, length);
                arrived.add(message);
            }
        } finally {
            closed = true;
            connected = false;
            try {
                joined.close();
            } catch (IOException ignored) {
                // It is closed either way.
            }
        }
    }

    /** Whether the two are joined; whether the line is over; and, if it ended badly, why. */
    boolean connected() { return connected; }
    boolean closed() { return closed; }
    String problem() { return problem; }

    /** Sends a message of a kind. One that says how the fight stands replaces any such that has not gone yet. */
    void send(int kind, byte[] content) {
        if (closed) return;
        byte[] message = new byte[content.length + 1];
        message[0] = (byte) kind;
        System.arraycopy(content, 0, message, 1, content.length);
        if (kind == STATE) standing.set(message);
        else going.add(message);
    }

    void send(int kind, int... content) {
        byte[] bytes = new byte[content.length];
        for (int index = 0; index < content.length; index++) bytes[index] = (byte) content[index];
        send(kind, bytes);
    }

    /** The next message that has arrived, its kind first, or null if none has. */
    byte[] poll() { return arrived.poll(); }

    /** Ends the line: says goodbye if it can, and lets go of the network. */
    void close() {
        if (!closed && connected) {
            try {
                Socket open = socket;
                if (open != null) {
                    // Straight out, since the thread that writes is about to stop.
                    Thread parting = new Thread(() -> {
                        try {
                            open.getOutputStream().write(new byte[]{BYE, 0, 0});
                            open.close();
                        } catch (IOException ignored) {
                            // The other end will notice the silence.
                        }
                    }, "brawl-bye");
                    parting.setDaemon(true);
                    parting.start();
                }
            } catch (RuntimeException ignored) {
                // Nothing to say goodbye with.
            }
        }
        closed = true;
        connected = false;
        ServerSocket waiting = server;
        if (waiting != null) {
            Thread shutting = new Thread(() -> {
                try {
                    waiting.close();
                } catch (IOException ignored) {
                    // It was closed already.
                }
            }, "brawl-shut");
            shutting.setDaemon(true);
            shutting.start();
        }
    }
}
