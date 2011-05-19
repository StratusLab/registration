/*
 Created as part of the StratusLab project (http://stratuslab.eu),
 co-funded by the European Commission under the Grant Agreement
 INFSO-RI-261552.

 Copyright (c) 2011, Centre National de la Recherche Scientifique (CNRS)

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package eu.stratuslab.registration.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class UserAttributeTest {

    @Test
    public void checkAttrToKeyToAttrCycle() {

        for (UserAttribute attr : UserAttribute.values()) {
            UserAttribute otherAttr = UserAttribute.valueWithKey(attr.key);
            assertEquals(attr, otherAttr);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidKey() {
        UserAttribute.valueWithKey("unknown_key");
    }

    @Test
    public void invalidUsernames() {

        String[] invalidUsernames = new String[] { "a", "aa",
                "123456789012345678901", "bad%char", "bad space", "bad.char" };

        for (String username : invalidUsernames) {
            assertFalse(UserAttribute.isValidUsername(username));
        }

    }

    @Test
    public void validUsernames() {

        String[] validUsernames = new String[] { "aaa", "12345678901234567890",
                "all_ok" };

        for (String username : validUsernames) {
            assertTrue(UserAttribute.isValidUsername(username));
        }

    }

    @Test
    public void invalidEmailAddresses() {

        String[] invalidEmailAddresses = new String[] { "a@b@c", "alpha@",
                "@beta", "alpha" };

        for (String emailAddress : invalidEmailAddresses) {
            assertFalse(UserAttribute.isValidEmailAddress(emailAddress));
        }

    }

    @Test
    public void validEmailAddresses() {

        String[] validEmailAddresses = new String[] { "a@example.com",
                "alpha_beta@example.org" };

        for (String emailAddress : validEmailAddresses) {
            assertTrue(UserAttribute.isValidEmailAddress(emailAddress));
        }

    }

    @Test
    public void emptyStrings() {

        String[] whitespaceStrings = new String[] { "", " ", "\t", "\f", "\n",
                "\r" };

        for (String s : whitespaceStrings) {
            assertFalse(UserAttribute.isNotWhitespace(s));
            assertTrue(UserAttribute.isWhitespace(s));
        }

    }

    @Test
    public void nonemptyStrings() {

        String[] whitespaceStrings = new String[] { "alpha", "alpha beta",
                "a\tb" };

        for (String s : whitespaceStrings) {
            assertTrue(UserAttribute.isNotWhitespace(s));
            assertFalse(UserAttribute.isWhitespace(s));
        }

    }

    @Test
    public void invalidPasswords() {

        String[] invalidPasswords = new String[] { "a", "aaaaaaa",
                "123456789012345678901", "bad space", "bad\tchar" };

        for (String password : invalidPasswords) {
            assertFalse(UserAttribute.isValidPassword(password));
        }

    }

    @Test
    public void validPasswords() {

        String[] validPasswords = new String[] { "aaaaaaaa",
                "12345678901234567890", "all_ok--", "all_ok.." };

        for (String password : validPasswords) {
            assertTrue(UserAttribute.isValidPassword(password));
        }

    }

    @Test
    public void invalidLdapNames() {
        String[] invalidLdapNames = new String[] { "", " ", "\t",
                "Invalid String" };

        for (String name : invalidLdapNames) {
            assertFalse(UserAttribute.isValidCertificateDN(name));
        }
    }

    @Test
    public void validLdapNames() {
        String[] validLdapNames = new String[] { "CN=Joe Smith",
                "CN=Jane Smith, C=US" };

        for (String name : validLdapNames) {
            assertTrue(UserAttribute.isValidCertificateDN(name));
        }
    }

}
