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
package eu.stratuslab.registration.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class HashUtilsTest {

    @Test
    public void hashAndCompare() {

        String clearTextPassword = "unsafe";

        String sshaHashedPassword = HashUtils.sshaHash(clearTextPassword);

        boolean compare = HashUtils.comparePassword(clearTextPassword,
                sshaHashedPassword);

        assertTrue(compare);
    }

    @Test
    public void falseWithNullClearTextPassword() {

        String clearTextPassword = "unsafe";

        String sshaHashedPassword = HashUtils.sshaHash(clearTextPassword);

        boolean compare = HashUtils.comparePassword(null, sshaHashedPassword);

        assertFalse(compare);
    }

    @Test
    public void falseWithNullHashedPassword() {

        String clearTextPassword = "unsafe";

        boolean compare = HashUtils.comparePassword(clearTextPassword, null);

        assertFalse(compare);
    }

    @Test
    public void falseWithTwoNullPasswords() {

        boolean compare = HashUtils.comparePassword(null, null);

        assertFalse(compare);
    }

    @Test(expected = IllegalArgumentException.class)
    public void errorFromNonhashedPassword() {

        String clearTextPassword = "ok";
        String hashedPassword = clearTextPassword;

        HashUtils.comparePassword(clearTextPassword, hashedPassword);
    }

    @Test
    public void checkSaltSize() {

        byte[] salt = HashUtils.generateSalt();

        assertEquals(HashUtils.SALT_LENGTH, salt.length);
    }

    @Test
    public void checkFusedArraySize() {

        int size1 = 11;
        int size2 = 7;

        byte[] first = new byte[size1];
        for (int i = 0; i < size1; i++) {
            first[i] = (byte) i;
        }

        byte[] second = new byte[size2];
        for (int i = 0; i < size2; i++) {
            second[i] = (byte) (i + size1);
        }

        byte[] result = HashUtils.fuseByteArrays(first, second);

        assertEquals(size1 + size2, result.length);

        for (int i = 0; i < result.length; i++) {
            assertEquals(result[i], (byte) i);
        }
    }

}
