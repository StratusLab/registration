#!/bin/bash -x

HOST_CN=`hostname -f`
CERT_PSWD='XYZXYZ'

cat > /tmp/openldap-openssl-ca.cfg <<EOF
[ req ]
default_bits           = 2048
default_md             = sha1
distinguished_name     = req_dn
x509_extensions        = v3_ca
prompt                 = no
input_password         = ${CERT_PSWD}
output_password        = ${CERT_PSWD}

dirstring_type = nobmp

[ req_dn ]
O = CLOUD
OU = OpenLDAP
CN = CA

[ v3_ca ]
subjectKeyIdentifier=hash
authorityKeyIdentifier=keyid,issuer
basicConstraints = CA:true

EOF

cat > /tmp/openldap-openssl-user.cfg <<EOF
[ req ]
default_bits           = 2048
default_md             = sha1
distinguished_name     = req_dn_user
x509_extensions        = v3_user
prompt                 = no
input_password         = ${CERT_PSWD}
output_password        = ${CERT_PSWD}

dirstring_type = nobmp

[ req_dn_user ]
O = CLOUD
OU = OpenLDAP
CN = ${HOST_CN}

[ v3_user ]
subjectKeyIdentifier=hash
authorityKeyIdentifier=keyid,issuer
basicConstraints = CA:false

EOF

# Create a certificate signing request.
openssl req -new \
        -keyout cakey.pem -out ca.csr \
        -passout pass:${CERT_PSWD} \
        -config /tmp/openldap-openssl-ca.cfg
chmod 0600 cakey.pem

# Create root CA certificate. 
openssl x509 -req -days 365 \
             -in ca.csr -out cacrt.pem \
             -signkey cakey.pem \
             -extfile /tmp/openldap-openssl-ca.cfg -extensions v3_ca \
             -passin pass:${CERT_PSWD}

# Create a certificate signing request for service certificate.
# The private key must NOT be encrypted. 
openssl req -new \
            -keyout serverkey.pem -out server.csr \
            -nodes \
            -config /tmp/openldap-openssl-user.cfg
chmod 0600 serverkey.pem
chown ldap:ldap serverkey.pem

# Create server certificate.
openssl x509 -req -days 364 \
             -in server.csr -out servercrt.pem \
             -CA cacrt.pem -CAkey cakey.pem -set_serial 01 \
             -extfile /tmp/openldap-openssl-user.cfg -extensions v3_user \
             -passin pass:${CERT_PSWD}

# Put the CA certificate into a truststore for Java.
rm -f cacrt.jks
keytool -importcert -noprompt -trustcacerts -alias CA_${HOST_CN} \
        -file cacrt.pem -keystore cacrt.jks -storepass ${CERT_PSWD}

# Clean up intermediate files.
rm -f *.csr *.srl 

# Verify that the chain actually works.
openssl verify -CAfile cacrt.pem servercrt.pem 
