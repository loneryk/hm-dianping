package com.hmdp.controller;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertTrue;

class UploadControllerTest {

    @Test
    void createNewFileName_shouldUseFallbackExtensionForMissingOrInvalidNames() throws Exception {
        UploadController controller = new UploadController();
        Method method = UploadController.class.getDeclaredMethod("createNewFileName", String.class);
        method.setAccessible(true);

        String missing = (String) method.invoke(controller, (Object) null);
        String noExtension = (String) method.invoke(controller, "avatar");
        String invalidSuffix = (String) method.invoke(controller, "avatar.undefined");
        String valid = (String) method.invoke(controller, "avatar.png");

        assertTrue(missing.endsWith(".jpg"));
        assertTrue(noExtension.endsWith(".jpg"));
        assertTrue(invalidSuffix.endsWith(".jpg"));
        assertTrue(valid.endsWith(".png"));
    }
}
