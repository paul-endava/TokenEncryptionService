package com.br.crypto;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;

import java.util.Optional;

public class TestResultLogger implements TestWatcher {

    @Override
    public void testSuccessful(ExtensionContext context) {
        System.out.println("✅ SUCCESS: " + context.getDisplayName());
    }

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        System.out.println("❌ FAILED: " + context.getDisplayName());
        System.out.println("   Reason: " + cause.getMessage());
    }

    @Override
    public void testAborted(ExtensionContext context, Throwable cause) {
        System.out.println("⚠️  ABORTED: " + context.getDisplayName());
        System.out.println("   Reason: " + cause.getMessage());
    }

    @Override
    public void testDisabled(ExtensionContext context, Optional<String> reason) {
        System.out.println("⏭️  DISABLED: " + context.getDisplayName());
        reason.ifPresent(r -> System.out.println("   Reason: " + r));
    }
}