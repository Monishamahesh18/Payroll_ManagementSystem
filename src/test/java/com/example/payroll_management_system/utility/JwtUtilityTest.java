package com.example.payroll_management_system.utility;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

public class JwtUtilityTest {

    private JwtUtility jwtUtility;

    @BeforeEach
    public void setUp(){
        // jwt.secret comes from application.properties. here we set it ourself
        jwtUtility = new JwtUtility();
        ReflectionTestUtils.setField(jwtUtility, "secretKey", "unit-test-secret-key-1234567890");
    }

    @Test
    public void generateTokenTest(){
        String token = jwtUtility.generateToken("ravi@easypay.com");

        assertEquals("ravi@easypay.com", jwtUtility.extractUsername(token));
        assertTrue(jwtUtility.validateToken(token, "ravi@easypay.com"));
    }

    @Test
    public void validateTokenTest_AnotherUser(){
        String token = jwtUtility.generateToken("ravi@easypay.com");

        assertFalse(jwtUtility.validateToken(token, "anita@easypay.com"));
    }

    @Test
    public void tokenWithAnotherSecretTest(){
        JwtUtility other = new JwtUtility();
        ReflectionTestUtils.setField(other, "secretKey", "some-other-secret-key-0987654321");
        String token = other.generateToken("ravi@easypay.com");

        assertThrows(SignatureException.class, () -> jwtUtility.extractUsername(token));
    }

    @Test
    public void changedTokenTest(){
        String token = jwtUtility.generateToken("ravi@easypay.com");
        // change one character inside the token
        int position = token.indexOf('.') + 3;
        char changed = token.charAt(position) == 'A' ? 'B' : 'A';
        String tampered = token.substring(0, position) + changed + token.substring(position + 1);

        assertThrows(JwtException.class, () -> jwtUtility.extractUsername(tampered));
    }
}
