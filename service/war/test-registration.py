#!/usr/bin/env python

#
# Created as part of the StratusLab project (http://stratuslab.eu),
# co-funded by the European Commission under the Grant Agreement
# INFSO-RI-261552."
#
# Copyright (c) 2011, Centre National de la Recherche Scientific (CNRS)
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

import ldap
import ldap.modlist as modlist

baseUrl = 'https://localhost:8444/'
ldapUrl = 'ldap://localhost:10389/'
userDn = 'jsmith'
userPassword = 'dummy_password'
userPasswordNew = 'dummy_password1'

managerDn = 'uid=admin,ou=system'
managerPassword = 'secret'

usersBaseDn = 'ou=users,o=cloud'
groupsBaseDn = 'ou=groups,o=cloud'
actionsBaseDn = 'ou=actions,o=cloud'

def SearchByManager(baseDn):
    ldapObj = ldap.initialize(ldapUrl)
    ldapObj.simple_bind_s(managerDn, managerPassword)

    searchScope = ldap.SCOPE_SUBTREE
    searchFilter = 'objectClass=*'
    retrieveAttributes = None

    ldap_result_id = ldapObj.search(usersBaseDn, searchScope, searchFilter, retrieveAttributes)

    result_set = []

    while 1:
        result_type, result_data = ldapObj.result(ldap_result_id, 0)
        if (result_data == []):
            break
        else:
            if result_type == ldap.RES_SEARCH_ENTRY:
                result_set.append(result_data)

    return result_set
    
try:

    result_set = SearchByManager(usersBaseDn)

    print result_set

except ldap.LDAPError, e:
    print e
    # handle error however you like
    
