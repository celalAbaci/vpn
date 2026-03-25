package de.blinkt.openvpn.core;

import android.os.Build;
import androidx.annotation.NonNull;
import java.math.BigInteger;
import java.net.Inet6Address;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;

public class NetworkSpace {

    static class IpAddress implements Comparable<IpAddress> {
        private BigInteger netAddress;
        public int networkMask;
        private boolean included;
        private boolean isV4;
        private BigInteger firstAddress;
        private BigInteger lastAddress;

        @Override
        public int compareTo(@NonNull IpAddress another) {
            int sort = getFirstAddress().compareTo(another.getFirstAddress());
            if (sort != 0)
                return sort;
            // Eşitse maskesi küçük olan (daha büyük ağ) önce gelir
            if (networkMask > another.networkMask)
                return -1;
            else if (networkMask < another.networkMask)
                return 1;
            return 0;
        }

        public IpAddress(CIDRIP ip, boolean include) {
            included = include;
            netAddress = BigInteger.valueOf(ip.getInt());
            networkMask = ip.len;
            isV4 = true;
            calculateStartEnd();
        }

        public IpAddress(Inet6Address ip, int mask, boolean include) {
            included = include;
            networkMask = mask;
            isV4 = false;
            int s = 128;
            netAddress = BigInteger.ZERO;
            for (byte b : ip.getAddress()) {
                s -= 8;
                netAddress = netAddress.add(BigInteger.valueOf((b & 0xFF)).shiftLeft(s));
            }
            calculateStartEnd();
        }

        public IpAddress(BigInteger baseAddress, int mask, boolean include, boolean v4) {
            netAddress = baseAddress;
            networkMask = mask;
            included = include;
            isV4 = v4;
            calculateStartEnd();
        }

        private void calculateStartEnd() {
            BigInteger maskVal;
            int dateBits;
            if (isV4) dateBits = 32;
            else dateBits = 128;

            if (networkMask == dateBits) {
                firstAddress = netAddress;
                lastAddress = netAddress;
            } else {
                int shift = dateBits - networkMask;
                firstAddress = netAddress.shiftRight(shift).shiftLeft(shift);
                BigInteger one = BigInteger.ONE;
                BigInteger allOnes = one.shiftLeft(shift).subtract(one);
                lastAddress = firstAddress.add(allOnes);
            }
        }

        public boolean containsNet(IpAddress network) {
            // Aynı türde olmalılar (v4/v6)
            if (isV4 != network.isV4) return false;

            // Başlangıç <= network.first VE Bitiş >= network.last
            return firstAddress.compareTo(network.getFirstAddress()) <= 0 &&
                    lastAddress.compareTo(network.getLastAddress()) >= 0;
        }

        public BigInteger getFirstAddress() { return firstAddress; }
        public BigInteger getLastAddress() { return lastAddress; }

        @NonNull
        @Override
        public String toString() {
            if (isV4) return String.format(Locale.US, "%d.%d.%d.%d/%d",
                    (netAddress.longValue() >> 24) & 0xFF,
                    (netAddress.longValue() >> 16) & 0xFF,
                    (netAddress.longValue() >> 8) & 0xFF,
                    netAddress.longValue() & 0xFF, networkMask);
            else {
                // Basitleştirilmiş IPv6 gösterimi
                return "IPv6/" + networkMask;
            }
        }

        public String getIPv4Address() {
            return String.format(Locale.US, "%d.%d.%d.%d",
                    (netAddress.longValue() >> 24) & 0xFF,
                    (netAddress.longValue() >> 16) & 0xFF,
                    (netAddress.longValue() >> 8) & 0xFF,
                    netAddress.longValue() & 0xFF);
        }

        public String getIPv6Address() {
            // IPv6 String çevrimi karmaşık, basitleştiriyoruz
            return "";
        }

        public android.net.IpPrefix getPrefix() throws java.net.UnknownHostException {
            // Android API seviyesine göre IpPrefix döndürür
            // Burada basitleştirmek için null dönebilir veya implemente edilebilir.
            // Bizim 'installRoutesExcluded' metodunda kullanılıyor ama onu iptal ettiğimiz için sorun yok.
            return null;
        }
    }

    private final TreeSet<IpAddress> mIpAddresses = new TreeSet<>();

    public Collection<IpAddress> getPositiveIPList() {
        TreeSet<IpAddress> copy = new TreeSet<>(mIpAddresses);
        return copy;
    }

    public void addIP(CIDRIP cidrIp, boolean include) {
        mIpAddresses.add(new IpAddress(cidrIp, include));
    }

    public void addIPv6(Inet6Address ip, int mask, boolean included) {
        mIpAddresses.add(new IpAddress(ip, mask, included));
    }

    public List<IpAddress> getNetworks(boolean included) {
        List<IpAddress> ips = new ArrayList<>();
        for (IpAddress ip : mIpAddresses) {
            if (ip.included == included) {
                ips.add(ip);
            }
        }
        return ips;
    }
}