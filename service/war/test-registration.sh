#!/bin/sh -x

baseurl=https://localhost:8444
ldapurl=ldap://localhost:10389
username=jsmith
password=dummy_password
new_password=dummy_password1

ldapmanager='uid=admin,ou=system'
ldappasswd=secret

CURL="curl --insecure --silent --show-error --fail"

LDAPSEARCH="ldapsearch -LLL -H ${ldapurl} -x -D ${ldapmanager} -w ${ldappasswd}"

#
# Check that static pages exist.
#
urls="/ /policies/ /register/ /reset/"
for url in $urls; do 
  $CURL ${baseurl}${url} 2>&1 >/dev/null
  rc=$?

  if [ $rc != 0 ]; then
    echo "ERROR: ${baseurl}${url}"
    exit 1
  fi
done

#
# Check that registration works.
#
register_url=${baseurl}/users/

$CURL $register_url \
 --data "uid=${username}" \
 --data 'mail=smith@example.org' \
 --data 'givenName=john' \
 --data 'sn=smith' \
 --data 'seeAlso=CN=John Smith,O=StratusLab' \
 --data "newUserPassword=${password}" \
 --data "newUserPasswordCheck=${password}" \
 --data 'message=I love clouds' \
 --data 'agreement=true'

rc=$?

if [ $rc != 0 ]; then
  echo "REGISTRATION ERROR: ${register_url}"
  exit 1
fi

#
# Check that profile is accessible.
#
profile_url=${baseurl}/profile/

$CURL ${profile_url} --user ${username}:${password} 2>&1 >/dev/null
rc=$?

if [ $rc != 0 ]; then
  echo "ERROR: ${profile_url}"
  exit 1
fi

#
# Check that reset password works. 
#
reset_url=${baseurl}/reset/

$CURL $reset_url \
 --data "uid=${username}"

rc=$?

if [ $rc != 0 ]; then
  echo "RESET ERROR: ${reset_url}"
  exit 1
fi

#
# Retrieve the action UUID.
#
uuid=`$LDAPSEARCH -b 'ou=actions,o=cloud' cn | grep cn: | cut -d ' ' -f 2`
echo $uuid

#
# Click on the reset link.
#
#action_url=${baseurl}/actions/${uuid}/

#$CURL $action_url

#rc=$?

#if [ $rc != 0 ]; then
#  echo "ACTION URL ERROR: ${action_url}"
#  exit 1
#fi

#
# Check that LDAP user entry can be found. 
#
$LDAPSEARCH -b 'ou=users,o=cloud' '(objectClass=inetOrgPerson)'

#
# Check that LDAP group entry can be found. 
#
$LDAPSEARCH -b 'ou=groups,o=cloud' '(objectClass=groupOfUniqueNames)'

