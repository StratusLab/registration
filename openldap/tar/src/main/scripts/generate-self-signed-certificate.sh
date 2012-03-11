#!/bin/bash -xe

HOST_CN=`hostname -f`
CERT_PSWD='XYZXYZ'

cat > /tmp/openldap-openssl-ca.cfg <<EOF
[ req ]
distinguished_name     = req_dn
x509_extensions        = v3_ca
prompt                 = no
input_password         = ${CERT_PSWD}
output_password        = ${CERT_PSWD}

dirstring_type = nobmp

[ req_dn ]
O = cloud
OU = openldap
CN = test certificate authority

[ v3_ca ]
subjectKeyIdentifier=hash
authorityKeyIdentifier=keyid:always,issuer
basicConstraints = CA:true

EOF

cat > /tmp/openldap-openssl-user.cfg <<EOF
[ req ]
distinguished_name     = req_dn_user
x509_extensions        = v3_user
prompt                 = no
input_password         = ${CERT_PSWD}
output_password        = ${CERT_PSWD}

dirstring_type = nobmp

[ req_dn_user ]
O = cloud
OU = openldap
CN = ${HOST_CN}

[ v3_user ]
basicConstraints = CA:false
nsCertType=client, email, objsign
keyUsage=critical, digitalSignature, nonRepudiation, keyEncipherment, dataEncipherment, keyAgreement
subjectKeyIdentifier=hash
authorityKeyIdentifier=keyid:always,issuer

EOF

# Generate initial private key.
openssl genrsa -passout pass:${CERT_PSWD} -des3 -out cakey.pem 2048
chmod 0600 cakey.pem

# Create a certificate signing request.
openssl req -new -key cakey.pem -out ca.csr \
        -config /tmp/openldap-openssl-ca.cfg

# Create root CA certificate. 
openssl x509 -req -days 365 \
             -in ca.csr -out cacrt.pem \
             -signkey cakey.pem \
             -extfile /tmp/openldap-openssl-ca.cfg -extensions v3_ca \
             -passin pass:${CERT_PSWD}

# Generate a private key for the server.
openssl genrsa -passout pass:${CERT_PSWD} -des3 -out serverkey.pem 2048
chmod 0600 serverkey.pem 

# Create a certificate signing request.
openssl req -new -nodes -key serverkey.pem -out server.csr \
            -config /tmp/openldap-openssl-user.cfg

# Create server certificate.
openssl x509 -req -days 364 \
             -in server.csr -out servercrt.pem \
             -CA cacrt.pem -CAkey cakey.pem -set_serial 01 \
             -extfile /tmp/openldap-openssl-user.cfg \
             -passin pass:${CERT_PSWD}

# Clean up intermediate files.
rm -f *.csr

# Verify that the chain actually works.
openssl verify -CAfile cacrt.pem servercrt.pem 
