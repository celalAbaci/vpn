-- 🛠️ 1. AYARLAR: Şemayı seçiyoruz
SET search_path TO vpn_project, public;

-- 🛠️ 2. ÖNCE ÜLKE EKLE
INSERT INTO countries (country_name, country_code, created_at, updated_at)
VALUES ('Netherlands', 'NL', NOW(), NOW())
ON CONFLICT (country_code) DO NOTHING;

-- ---------------------------------------------------------
-- ADIM 1: VPN SUNUCUSUNU EKLE
-- ---------------------------------------------------------
INSERT INTO vpn_servers (
    server_name,
    server_ip_address,
    country_id,
    is_active,
    current_load_percentage,
    current_connected_users,
    ssh_username,
    ssh_password,
    ssh_port,
    created_at,
    updated_at
) VALUES (
             'Amsterdam Public 1',
             '188.166.31.42',
             (SELECT id FROM countries WHERE country_code = 'NL' LIMIT 1),
             true,
             0.0,
             0,
             'root',
             'admin123',
             22,
             NOW(),
             NOW()
         ) ON CONFLICT (server_ip_address) DO NOTHING;

-- ---------------------------------------------------------
-- ADIM 2: TEST KULLANICISI EKLE
-- ---------------------------------------------------------
INSERT INTO users (
    username,
    email,
    password_hash,
    role,
    is_enabled,
    created_at,
    updated_at
) VALUES (
             'testuser',
             'test@vpn.com',
             '$2a$10$DUMMYHASHFORPASSWORD',
             'USER',
             true,
             NOW(),
             NOW()
         ) ON CONFLICT (username) DO NOTHING;

-- ---------------------------------------------------------
-- ADIM 3: CONFIG DOSYASINI EKLE (user_vpn_configs)
-- "ON CONFLICT" kaldırıldı, doğrudan ekleme yapacak.
-- ---------------------------------------------------------
INSERT INTO user_vpn_configs (
    user_id,
    server_id,
    identifier_key,
    protocol,
    config_content,
    created_at,
    updated_at
) VALUES (
             (SELECT id FROM users WHERE username = 'testuser' LIMIT 1),
             (SELECT id FROM vpn_servers WHERE server_ip_address = '188.166.31.42' LIMIT 1),
             'amsterdam_config_1',
             'OPENVPN',
             $$client
dev tun
proto tcp
remote 188.166.31.42 443
resolv-retry infinite
nobind
persist-key
persist-tun
remote-cert-tls server
auth SHA512
ignore-unknown-option block-outside-dns
verb 3

<cert>
Certificate:
    Data:
        Version: 3 (0x2)
        Serial Number:
            dc:80:6c:11:4e:41:a9:dd:ff:fe:56:14:d6:f3:0e:7a
        Signature Algorithm: sha256WithRSAEncryption
        Issuer: CN=Easy-RSA CA
        Validity
            Not Before: Nov 21 19:13:37 2025 GMT
            Not After : Nov 19 19:13:37 2035 GMT
        Subject: CN=Telefon
        Subject Public Key Info:
            Public Key Algorithm: rsaEncryption
                Public-Key: (2048 bit)
                Modulus:
                    00:d0:a5:fb:46:72:2e:08:4a:94:c3:f5:70:41:d2:
                    10:01:b5:7d:db:ef:78:68:ec:6a:0c:b0:d2:ce:cf:
                    f0:8e:2c:4a:0d:b2:55:59:0c:6f:5f:ba:86:be:21:
                    64:af:56:70:c9:73:14:e7:03:4b:b3:2a:58:33:ce:
                    f5:52:48:98:6a:93:da:aa:01:f7:2c:1e:6b:52:4b:
                    34:8e:23:ab:8b:dd:4d:68:b7:22:53:b5:4f:c8:e5:
                    58:ae:36:66:07:77:67:cb:8d:0c:c3:f3:0b:40:9f:
                    25:83:ed:a7:11:66:ae:f4:59:f1:53:0e:f5:19:af:
                    3e:35:f8:cb:07:74:1d:0a:44:6e:a6:b5:23:93:8f:
                    01:30:5c:71:5d:55:12:f9:21:ab:24:62:eb:86:46:
                    60:4f:bb:b7:57:52:42:5e:0f:0b:cf:a7:e5:f5:a3:
                    11:02:6f:f5:aa:97:bf:19:6a:4c:1b:f0:47:39:4a:
                    34:1c:5b:91:1c:4b:da:97:15:5d:ab:4b:52:a5:a1:
                    3c:6c:c2:93:cc:af:08:f0:d7:45:a2:1b:be:b1:d2:
                    e8:8b:8c:11:d2:c0:3f:65:d7:34:5a:fb:8c:5e:2d:
                    f0:4c:98:b5:71:f6:17:e6:57:de:f2:87:c8:01:99:
                    ad:d5:a9:77:4e:8d:fb:94:8e:44:9f:d8:e9:00:bf:
                    2c:8d
                Exponent: 65537 (0x10001)
        X509v3 extensions:
            X509v3 Basic Constraints:
                CA:FALSE
            X509v3 Subject Key Identifier:
                8A:1B:43:78:E0:91:CD:E7:33:DC:30:E9:D6:51:4F:06:75:84:52:7D
            X509v3 Authority Key Identifier:
                keyid:C5:9C:EC:D1:50:09:15:4D:2C:B7:DA:3C:82:49:CB:35:7A:E1:EA:4B
                DirName:/CN=Easy-RSA CA
                serial:7B:C1:B8:E0:BE:91:E4:45:5A:0C:88:C7:08:35:B2:EA:58:C7:12:37
            X509v3 Extended Key Usage:
                TLS Web Client Authentication
            X509v3 Key Usage:
                Digital Signature
    Signature Algorithm: sha256WithRSAEncryption
    Signature Value:
        40:73:a3:37:17:43:e6:51:67:cc:af:2b:11:a2:5f:e1:e0:20:
        42:28:17:6d:0e:f6:e0:fb:d4:28:66:a1:c1:6d:27:3f:b0:9e:
        78:a8:e9:3a:7a:c4:72:7a:a7:5b:99:b9:60:b5:9d:9a:90:dd:
        a2:97:b3:cc:d5:8a:5a:39:49:96:d4:f9:a8:45:0d:84:b5:08:
        31:a9:2d:7e:8d:11:a9:8a:19:62:a3:d2:86:f7:bf:50:ef:41:
        85:aa:a2:fa:ae:d0:da:3a:11:19:70:a3:88:d0:d6:28:9c:65:
        88:4e:84:6b:3b:5e:f3:27:b0:07:38:c2:b7:7f:64:30:b4:e6:
        5b:c1:54:98:94:d1:e5:4f:ba:86:3e:0c:61:65:96:c2:18:cf:
        20:1b:c4:13:e4:18:82:ba:7f:26:33:89:30:79:2d:b6:d5:d9:
        dd:28:bb:df:3b:04:4e:95:3c:7a:63:b0:8a:38:64:b3:ff:49:
        b5:ad:81:76:4e:95:05:4b:af:23:d3:20:51:ae:be:f8:12:89:
        02:55:d7:cd:9f:37:3a:e0:b8:3a:8e:08:e1:f3:33:ea:29:b8:
        6b:97:0b:e3:3e:d1:e9:99:49:0b:26:2d:70:d0:22:69:bb:77:
        59:62:70:f4:53:e5:ef:f3:ed:8c:d3:bc:00:c7:f8:68:70:9f:
        59:d6:59:73
-----BEGIN CERTIFICATE-----
MIIDVjCCAj6gAwIBAgIRANyAbBFOQand//5WFNbzDnowDQYJKoZIhvcNAQELBQAw
FjEUMBIGA1UEAwwLRWFzeS1SU0EgQ0EwHhcNMjUxMTIxMTkxMzM3WhcNMzUxMTE5
MTkxMzM3WjASMRAwDgYDVQQDDAdUZWxlZm9uMIIBIjANBgkqhkiG9w0BAQEFAAOC
AQ8AMIIBCgKCAQEA0KX7RnIuCEqUw/VwQdIQAbV92+94aOxqDLDSzs/wjixKDbJV
WQxvX7qGviFkr1ZwyXMU5wNLsypYM871UkiYapPaqgH3LB5rUks0jiOri91NaLci
U7VPyOVYrjZmB3dny40Mw/MLQJ8lg+2nEWau9FnxUw71Ga8+NfjLB3QdCkRuprUj
k48BMFxxXVUS+SGrJGLrhkZgT7u3V1JCXg8Lz6fl9aMRAm/1qpe/GWpMG/BHOUo0
HFuRHEvalxVdq0tSpaE8bMKTzK8I8NdFohu+sdLoi4wR0sA/Zdc0WvuMXi3wTJi1
cfYX5lfe8ofIAZmt1al3To37lI5En9jpAL8sjQIDAQABo4GiMIGfMAkGA1UdEwQC
MAAwHQYDVR0OBBYEFIobQ3jgkc3nM9ww6dZRTwZ1hFJ9MFEGA1UdIwRKMEiAFMWc
7NFQCRVNLLfaPIJJyzV64epLoRqkGDAWMRQwEgYDVQQDDAtFYXN5LVJTQSBDQYIU
e8G44L6R5EVaDIjHCDWy6ljHEjcwEwYDVR0lBAwwCgYIKwYBBQUHAwIwCwYDVR0P
BAQDAgeAMA0GCSqGSIb3DQEBCwUAA4IBAQBAc6M3F0PmUWfMrysRol/h4CBCKBdt
Dvbg+9QoZqHBbSc/sJ54qOk6esRyeqdbmblgtZ2akN2il7PM1YpaOUmW1PmoRQ2E
tQgxqS1+jRGpihlio9KG979Q70GFqqL6rtDaOhEZcKOI0NYonGWIToRrO17zJ7AH
OMK3f2QwtOZbwVSYlNHlT7qGPgxhZZbCGM8gG8QT5BiCun8mM4kweS221dndKLvf
OwROlTx6Y7CKOGSz/0m1rYF2TpUFS68j0yBRrr74EokCVdfNnzc64Lg6jgjh8zPq
KbhrlwvjPtHpmUkLJi1w0CJpu3dZYnD0U+Xv8+2M07wAx/hocJ9Z1llz
-----END CERTIFICATE-----
</cert>

<key>
-----BEGIN PRIVATE KEY-----
MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDQpftGci4ISpTD
9XBB0hABtX3b73ho7GoMsNLOz/COLEoNslVZDG9fuoa+IWSvVnDJcxTnA0uzKlgz
zvVSSJhqk9qqAfcsHmtSSzSOI6uL3U1otyJTtU/I5ViuNmYHd2fLjQzD8wtAnyWD
7acRZq70WfFTDvUZrz41+MsHdB0KRG6mtSOTjwEwXHFdVRL5IaskYuuGRmBPu7dX
UkJeDwvPp+X1oxECb/Wql78Zakwb8Ec5SjQcW5EcS9qXFV2rS1KloTxswpPMrwjw
10WiG76x0uiLjBHSwD9l1zRa+4xeLfBMmLVx9hfmV97yh8gBma3VqXdOjfuUjkSf
2OkAvyyNAgMBAAECggEACZ/4HQicKJqOjXhlzhax0YjDXlq7PaI9eIn/orXjU7hy
ghQKU91HyS0b+7e9CUmXhY0SSpKgWPEVhVS0UTAzUVdL7Jb10mP+cQFlhaXayzNR
cTv7EJl6LYfggcjVbaq+1jXF5pQjnlhSrRwda0DgktN2im2HAo3ojlzRVAd+Zdn4
G6zTYpJ8r92mwvPm7y/XZ33zoZ24egvYjLHJg9lsXpLIoSJCCGd6mHgZn45Z1jH6
oQd19pzYsCGrD0PVcEJJ7eoh4U9wfPnkyqBmaJbNTdO3ZVJDwa3YTRd/yNtBZU8K
yyduhgF2R0SNhRvukVgy6PMdx4nzD2U4ng5hLbfnAQKBgQD5CnL4+2wJJnunbHfZ
X7fLFZEb6JAD1K1yUwV+8zEqRxA0lVvIKngT60DI+8JE03U6ItfcHQHGxqHgg+Ho
rfjMUJmdQ4GINnNeNakLd04WIeA3w8oFscDBpeDO5M0qK/0W56Uv0winbBP1CaZh
XQVzOCyvDceTEAgNyO1F+XA4cQKBgQDWepQ2+XW+3h8Xyz6dZNA36LN3/lWmixVd
snjP8l/cYqkaP7h8TlKJ47FJ4AM6UKAauGDQ4NTwMBIH0MVzbHGRoMLP+6/bDD4X
ag8uXStSaG9v3NIliYZnFAxWqeHqG6nC68YyCju52VbzUhj6Ns399LQ8V7Hu34v4
pSWN0Rcj3QKBgQDchJsfywlz5MaTjqFlMrN9OWCfdMOpELZwyADS06bSR9CarzJx
QOHIyYQ5M2xnI1LnIpk/R9Qd3h7SvhTYwsvOpwQO1AqrwYNQhXVlOMRFJsPyvQRz
HF2RWz+AE+tzjzXxBTco7NX54eKeFGhmSEceKjyu2SXCG+z1v9gSIPh0kQKBgQDP
dlih6KGVk8QdvvJ9SsQCIKxg+HqfE1AChx1HgFprkl+iTVrFmvlJ+h5GqFJW+SA1
WH4F7kTrZIQ8XRxxbsdYJ1np1rofN3NkMKooneTG3NBWMxJvTtzcHk9lEQS+8C1A
p9HaG330YqAx0wroFVUiPq6M5ALwXfpF9HEkAcnRXQKBgG4GnsHgmyWc1nN5QBTR
Oq4Z/WaatMEnQu7A5V52pPTajKGqXFJZOF+Cv/AiSjKaPmYezKnMMckk4gKnEcoy
G9P93fUJNsiwC9wHdsnomfOJ/JdEkSjRKEeWShrU40SLkhwQR6TRKEbCg1SJNu1f
e4g69olbtB0rKKgv5/limZI7
-----END PRIVATE KEY-----
</key>

<ca>
-----BEGIN CERTIFICATE-----
MIIDSzCCAjOgAwIBAgIUe8G44L6R5EVaDIjHCDWy6ljHEjcwDQYJKoZIhvcNAQEL
BQAwFjEUMBIGA1UEAwwLRWFzeS1SU0EgQ0EwHhcNMjUxMTIxMTkxMzM2WhcNMzUx
MTE5MTkxMzM2WjAWMRQwEgYDVQQDDAtFYXN5LVJTQSBDQTCCASIwDQYJKoZIhvcN
AQEBBQADggEPADCCAQoCggEBAKiE7PNn6o91OZQ0Ob3jD2y+8pIGF0y2Kiv1CZXI
PkRrtzpODKzTmvRe/FNEA6lYWEcQqF/BmFcvrbazc9+pyXtKOT+ni2f32b9TBHr2
6tDvCPBpsevmng7zhCN+zBM3J7yuiKM4R4F8nTL8PpRD/ySYN12metET8tGQweJ7
jfDu4/IMfhelNsEjcKbva4WwluXrycQnnrJf+NJXOl3KWfrIsSeGsOxkUAR00WfM
jFN/zI9qlp7fxJQnPNGQJA4KCj6mQ8F7h37/qK15vERePqB4gID5U7/EmlIcWzFw
7Ebfid98zYbdfJPxU54VszM2cRlcOWEHJW+pMieGvNKANbkCAwEAAaOBkDCBjTAM
BgNVHRMEBTADAQH/MB0GA1UdDgQWBBTFnOzRUAkVTSy32jyCScs1euHqSzBRBgNV
HSMESjBIgBTFnOzRUAkVTSy32jyCScs1euHqS6EapBgwFjEUMBIGA1UEAwwLRWFz
eS1SU0EgQ0GCFHvBuOC+keRFWgyIxwg1supYxxI3MAsGA1UdDwQEAwIBBjANBgkq
hkiG9w0BAQsFAAOCAQEAZ27S6GnvrpxWFRIedWBhwy8+L2SCBd9sK3j0iO5UZbZN
rKLeGljIE3HOJMy9yRVmlTwOwXPzMh4VkOfvB4+aMI2uJmJGmGBGVgDEEcYE96hH
z/Zs+dq3BKmfaEcE6VYjYN/ANNjv/5fZuK1H0OGbTwsLvktK6ICN4MpB032W2GXJ
cYOth8SFEsyZFwlsxlWBSRlk29JLhR7iZUDPSyl2xwpBUIY9z8Kl4u3Ra8v55V/B
FbqzU7XvUhtWRd13hJVvTiiCimk+mpbafNRy0eCNnPlv2mwgPnqaFMDJa1lHRizy
RezLtY8mKK10yjI7gReJkPX3d9Sl/cCd1ioR2uksow==
-----END CERTIFICATE-----
</ca>

<tls-crypt>
-----BEGIN OpenVPN Static key V1-----
8a8f605d05558e4d9e51901352d8a238
0de5f9a48665cc8d5db2d6646ebb3b00
5f5f40612ac448cd202cad882e9d3606
d4e24c70573167557d98802fc1810348
3edf1f76a1adb869f10a10f76bd9d50e
7ed463a73ee8fb21b1485e001cd7f15e
404c056389a1b0e87023fc87f499e316
f468a91866b58a0d9c2590fecf0ffd60
e0133e1796ae543472b79ba61cbf7e82
7e04f305688a695dabdbc9c3a9e42d64
68d85ea66112ca4989a4223d403ae02a
9f3d920306a08694656a00bc0f0a0d24
d9a935f535b7002ed7e355922be1f5c5
c52ed79fe0dd12731a0fec51ab4b7f1b
65ae083ddbee3a1591108a8d92ee2489
f2b739f16413e332c3d6568306a7a727
-----END OpenVPN Static key V1-----
</tls-crypt>$$,
             NOW(),
             NOW()
         );