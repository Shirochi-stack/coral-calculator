package com.coral.calculator;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;

/**
 * How two devices on the same Wi-Fi find each other for Page brawl, with no address to type:
 * the one that hosts announces a game on the local network, and the one that joins looks
 * for such an announcement and learns where to connect. Nothing leaves the local network.
 */
final class FightNearby {
    private static final String KIND = "_halgakosbrawl._tcp.";
    private static final String NAME = "Page brawl";

    private final NsdManager manager;
    private NsdManager.RegistrationListener announcing;
    private NsdManager.DiscoveryListener looking;
    /** Where a game was found, once one has been. */
    private volatile String address;
    private volatile int port;
    private volatile boolean resolving;

    FightNearby(Context context) {
        manager = (NsdManager) context.getApplicationContext().getSystemService(Context.NSD_SERVICE);
    }

    /** Announces a game waiting on a port. */
    void announce(int onPort) {
        if (manager == null || announcing != null) return;
        NsdServiceInfo game = new NsdServiceInfo();
        game.setServiceName(NAME);
        game.setServiceType(KIND);
        game.setPort(onPort);
        announcing = new NsdManager.RegistrationListener() {
            @Override public void onRegistrationFailed(NsdServiceInfo info, int error) {}
            @Override public void onUnregistrationFailed(NsdServiceInfo info, int error) {}
            @Override public void onServiceRegistered(NsdServiceInfo info) {}
            @Override public void onServiceUnregistered(NsdServiceInfo info) {}
        };
        try {
            manager.registerService(game, NsdManager.PROTOCOL_DNS_SD, announcing);
        } catch (RuntimeException refused) {
            announcing = null;
        }
    }

    /** Looks for a game announced on the local network. */
    void look() {
        if (manager == null || looking != null) return;
        looking = new NsdManager.DiscoveryListener() {
            @Override public void onStartDiscoveryFailed(String kind, int error) {}
            @Override public void onStopDiscoveryFailed(String kind, int error) {}
            @Override public void onDiscoveryStarted(String kind) {}
            @Override public void onDiscoveryStopped(String kind) {}
            @Override public void onServiceLost(NsdServiceInfo info) {}

            @Override public void onServiceFound(NsdServiceInfo info) {
                if (address != null || resolving || info.getServiceName() == null || !info.getServiceName().startsWith(NAME)) return;
                resolving = true;
                resolve(info);
            }
        };
        try {
            manager.discoverServices(KIND, NsdManager.PROTOCOL_DNS_SD, looking);
        } catch (RuntimeException refused) {
            looking = null;
        }
    }

    @SuppressWarnings("deprecation")
    private void resolve(NsdServiceInfo info) {
        try {
            manager.resolveService(info, new NsdManager.ResolveListener() {
                @Override public void onResolveFailed(NsdServiceInfo failed, int error) { resolving = false; }

                @Override public void onServiceResolved(NsdServiceInfo found) {
                    if (found.getHost() != null) {
                        port = found.getPort();
                        address = found.getHost().getHostAddress();
                    }
                    resolving = false;
                }
            });
        } catch (RuntimeException refused) {
            resolving = false;
        }
    }

    /** The address of the game that was found, or null while none has been; and its port. */
    String address() { return address; }
    int port() { return port; }

    /** Stops announcing and looking. */
    void stop() {
        if (manager == null) return;
        try {
            if (announcing != null) manager.unregisterService(announcing);
        } catch (RuntimeException ignored) {
            // It was not announced after all.
        }
        try {
            if (looking != null) manager.stopServiceDiscovery(looking);
        } catch (RuntimeException ignored) {
            // It was not looking after all.
        }
        announcing = null;
        looking = null;
    }
}
