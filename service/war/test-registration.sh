#!/bin/sh -x

baseurl=http://localhost:8080
username=jsmith
password=dummy_password

CURL="curl --silent --show-error --fail"

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


