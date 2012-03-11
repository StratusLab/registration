Configuring OpenLDAP for the Registration Service
=================================================

The registration service can run with any properly configured LDAP server.
However, OpenLDAP is available in most operating systems by default. This
README describes the process of configuring OpenLDAP for use with the
registration service. These instructions can also be used as a guideline for
configuring other servers.

General Requirements
--------------------

The registration service requires write access to an LDAP server to manage
user and group information. The tree of managed information is divided into
three parts: one for users, one for groups, and one to keep track of
asynchronous actions. The example files used below provide a detailed layout
of the database.

To verify users, an LDAP bind is used. Therefore, all users should have read
access to their own entry in the database. They should not have read access to
other parts of the information tree. They may have write access to certain
parts of their own entry, usually excluding the defined username and email
address.

Getting LDAP Daemon Running
---------------------------

Install the package "stratuslab-openldap-support". This provides a set of
files that will help with the OpenLDAP configuration and pulls in the required
dependencies: "openldap-clients", "openldap-servers", and "cyrus-sasl-ldap".
The SASL package is required to allow access to the LDAP configuration (kept
in the server itself) from the root account.

Edit the file /etc/sysconfig/ldap.  You should add the following lines:

    SLAPD_LDAP=no
    SLAPD_LDAPI=yes
    SLAPD_LDAPS=yes

These indicate what protocols will be supported by the server. The LDAPI
protocol will be required to configure the server. You can copy the file
/usr/share/stratuslab/registration-openldap/sysconfig/ldap to
/etc/sysconfig/ldap.

In recent versions of OpenLDAP all of the configuration is embedded in the
server and managed through the usual LDAP client commands. The exception is
the initial authorization for the root account via the ldapi protocol.

Search for the olcAccess line in the file
/etc/openldap/slapd.d/cn=config/olcDatabase={0}config.ldif (the braces are
part of the filename). Check that the attribute contains a value like the
following:

    olcAccess: {0}to * by dn.exact=gidNumber=0+uidNumber=0,cn=peercred,
     cn=external,cn=auth manage by * none     

If not, change the attribute value with your favorite editor. If split across
lines, be sure that the continuation lines start with a single space. This
will allow you to access the server's configuration from the root account.

Now start (or restart) the slapd daemon and try an initial search of the
server's configuration. The following should return the top-level
configuration of the server:

    $ ldapsearch -Y EXTERNAL -H ldapi:/// -LLL -s base -b cn=config

If this does not work correctly, review the above step and check for any
errors in /var/log/messages.

Server Configuration for LDAPS
------------------------------

Now that the service is running, we will configure the service for ldaps. If
you have a certificate for your server use that; if not, you can generate a
test certificate for initial tests. *NOTE: the key for the server's
certificate must not be encrpyted.*

The package contains a support script for generating a test certificate. This
will be a self-signed certificate that will serve as both the server's
certificate and the "certificate authority". A proper certificate signed by an
external certificate authority should be used for production. Run the
following command to generate the test certificates.

    /usr/share/stratuslab/registration-openldap/scripts/generate-self-signed-certificate.sh

This will generate three files: cacrt.pem, servercrt.pem, and serverkey.pem.
Verify that the serverkey.pem file is readable only by the user running the
slapd daemon (ldap:ldap usually).

Move these files (or the files from your real certificate) into the correct
places:

    $ cp cacrt.pem /etc/openldap/cacerts/
    $ cp serverkey.pem /etc/openldap/
    $ cp servercrt.pem /etc/openldap/

Modify the server's configuration to use these files with the following
command:

    $ ldapmodify -Y EXTERNAL -H ldapi:/// -f \
      /usr/share/stratuslab/registration-openldap/ldif/certificates-config.ldif

    SASL/EXTERNAL authentication started
    SASL username: gidNumber=0+uidNumber=0,cn=peercred,cn=external,cn=auth
    SASL SSF: 0
    modifying entry "cn=config"

This result of the command should indicate that the configuration was
successful. You *must restart* the server for the changes to take effect.

Client Configuration for LDAPS
------------------------------

The client *must* also be configured to use LDAPS. In particular, the server's
certificate must be available to the client. For a global configuration, put
the server's certificate into /etc/openldap/cacerts. Modify the configuration
file /etc/openldap/ldap.conf, adding the line

    TLS_CACERT /etc/openldap/cacerts/cacrt.pem

If a global configuration is not possible, then individual users can do the
same configuration in their own configuration files.

Creating Cloud Database
-----------------------

Now a database will need to be created for cloud authentication. An example
configuration is provided in
/usr/share/stratuslab/registration-openldap/ldif/cloud-db-defn.ldif. You
should make a copy of this file and change any parameters to suit your
installation. In particular, you *must change* the olcRootPW value.

You can generate a password using the slappasswd command. Type in your
password at the prompts and then replace the olcRootPW value with the given
hash.

Add this configuration to the server:

    $ ldapadd -Y EXTERNAL -H ldapi:/// -f cloud-db-defn.ldif

    SASL/EXTERNAL authentication started
    SASL username: gidNumber=0+uidNumber=0,cn=peercred,cn=external,cn=auth
    SASL SSF: 0
    adding new entry "cn=module,cn=config"

    adding new entry "olcDatabase=hdb,cn=config"

You should now be ready to add the skeleton of your database. Make a copy of
the file /usr/share/stratuslab/registration-openldap/ldif/cloud-data.ldif.
Modify the copy to suit your installation, in particular giving a value for
the administrator password.

Now add this skeleton to your database:

    $ ldapadd -x -H ldaps://onehost-5.lal.in2p3.fr -D cn=admin,o=cloud -W -f cloud-data.ldif
    Enter LDAP Password: 

    adding new entry "o=cloud"

    adding new entry "ou=users,o=cloud"

    adding new entry "ou=groups,o=cloud"

    adding new entry "ou=actions,o=cloud"

    adding new entry "cn=cloud-access,ou=groups,o=cloud"

The LDAP server is now ready to be used by the registration service.

If you have problems accessing the server via the ldaps protocol, you can
activate the plain ldap protocol to determine if the problem is with the
database configuration or the server's SSL configuration.
