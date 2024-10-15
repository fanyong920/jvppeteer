package com.ruiyun.jvppeteer.entities;

import java.util.List;


public class SecurityDetails {

        /**
         * Protocol name (e.g. "TLS 1.2" or "QUIC").
         */
        private String protocol;

        /**
         * Key Exchange used by the connection, or the empty string if not applicable.
         */
        private String keyExchange;

        /**
         * (EC)DH group used by the connection, if applicable.
         */
        private String keyExchangeGroup;

        /**
         * Cipher name.
         */
        private String cipher;

        /**
         * TLS MAC. Note that AEAD ciphers do not have separate MACs.
         */
        private String mac;

        /**
         * Certificate ID value.
         */
        private long certificateId;

        /**
         * Certificate subject name.
         */
        private String subjectName;

        /**
         * Subject Alternative Name (SAN) DNS names and IP addresses.
         */
        private List<String> sanList;

        /**
         * Name of the issuing CA.
         */
        private String issuer;

        /**
         * Certificate valid from date.
         */
        private long validFrom;

        /**
         * Certificate valid to (expiration) date
         */
        private long validTo;

        /**
         * List of signed certificate timestamps (SCTs).
         */
        private List<SignedCertificateTimestamp> signedCertificateTimestampList;

        /**
         * Whether the request complied with Certificate Transparency policy
         */
        private String certificateTransparencyCompliance;

        /**
         * The signature algorithm used by the server in the TLS server signature,
         * represented as a TLS SignatureScheme code point. Omitted if not
         * applicable or not known.
         */
        private int serverSignatureAlgorithm;

        /**
         * Whether the connection used Encrypted ClientHello
         */
        private boolean encryptedClientHello;

        public String getProtocol() {
            return protocol;
        }

        public void setProtocol(String protocol) {
            this.protocol = protocol;
        }

        public boolean getEncryptedClientHello() {
            return encryptedClientHello;
        }

        public void setEncryptedClientHello(boolean encryptedClientHello) {
            this.encryptedClientHello = encryptedClientHello;
        }

        public int getServerSignatureAlgorithm() {
            return serverSignatureAlgorithm;
        }

        public void setServerSignatureAlgorithm(int serverSignatureAlgorithm) {
            this.serverSignatureAlgorithm = serverSignatureAlgorithm;
        }

        public String getCertificateTransparencyCompliance() {
            return certificateTransparencyCompliance;
        }

        public void setCertificateTransparencyCompliance(String certificateTransparencyCompliance) {
            this.certificateTransparencyCompliance = certificateTransparencyCompliance;
        }

        public List<SignedCertificateTimestamp> getSignedCertificateTimestampList() {
            return signedCertificateTimestampList;
        }

        public void setSignedCertificateTimestampList(List<SignedCertificateTimestamp> signedCertificateTimestampList) {
            this.signedCertificateTimestampList = signedCertificateTimestampList;
        }

        public long getValidTo() {
            return validTo;
        }

        public void setValidTo(long validTo) {
            this.validTo = validTo;
        }

        public long getValidFrom() {
            return validFrom;
        }

        public void setValidFrom(long validFrom) {
            this.validFrom = validFrom;
        }

        public String getIssuer() {
            return issuer;
        }

        public void setIssuer(String issuer) {
            this.issuer = issuer;
        }

        public List<String> getSanList() {
            return sanList;
        }

        public void setSanList(List<String> sanList) {
            this.sanList = sanList;
        }

        public String getSubjectName() {
            return subjectName;
        }

        public void setSubjectName(String subjectName) {
            this.subjectName = subjectName;
        }

        public long getCertificateId() {
            return certificateId;
        }

        public void setCertificateId(long certificateId) {
            this.certificateId = certificateId;
        }

        public String getMac() {
            return mac;
        }

        public void setMac(String mac) {
            this.mac = mac;
        }

        public String getCipher() {
            return cipher;
        }

        public void setCipher(String cipher) {
            this.cipher = cipher;
        }

        public String getKeyExchangeGroup() {
            return keyExchangeGroup;
        }

        public void setKeyExchangeGroup(String keyExchangeGroup) {
            this.keyExchangeGroup = keyExchangeGroup;
        }

        public String getKeyExchange() {
            return keyExchange;
        }

        public void setKeyExchange(String keyExchange) {
            this.keyExchange = keyExchange;
        }
    }
