package com.sistema.ponto.biometria;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Interface com o ZKFinger SDK.
 * Esta classe fornece métodos estáticos que mapeiam diretamente para as funções nativas do SDK.
 */
public class FingerprintSensor {
    static {
        try {
            // Cria diretório temporário para as DLLs
            Path tempDir = Files.createTempDirectory("zkfinger");
            
            // Extrai e carrega as DLLs na ordem correta
            String[] dlls = {
                "libzkfp.dll",
                "libzkfpcsharp.dll",
                "libidfprcap.dll",
                "libsilkidcap.dll",
                "libzkfpmodulecap.dll",
                "libzklibcap.dll",
                "libcorrect.dll",
                "libsilkid.dll",
                "libzksensorcore.dll",
                "mi.dll",
                "USB.dll",
                "usb_dll.dll",
                "wd_utils.dll",
                "ZKFPCap_ASYNC.dll",
                "zkfputil.dll"
            };
            
            for (String dll : dlls) {
                extractAndLoadDll(tempDir, dll);
            }
            
            System.out.println("DLLs carregadas com sucesso!");
        } catch (Exception e) {
            System.err.println("Erro ao carregar biblioteca nativa: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private static void extractAndLoadDll(Path tempDir, String dllName) throws Exception {
        // Caminho da DLL no JAR
        String dllPath = "lib/dll/sdk/" + dllName;
        
        // Carrega a DLL do JAR
        InputStream in = FingerprintSensor.class.getClassLoader().getResourceAsStream(dllPath);
        if (in == null) {
            throw new RuntimeException("DLL não encontrada: " + dllPath);
        }

        // Extrai para o diretório temporário
        File tempFile = new File(tempDir.toFile(), dllName);
        try (OutputStream out = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        }

        // Carrega a DLL
        System.load(tempFile.getAbsolutePath());
    }

    // Métodos nativos que mapeiam diretamente para as funções do SDK
    public static native int Init();
    public static native void Terminate();
    public static native long OpenDevice(int index);
    public static native void CloseDevice(long handle);
    public static native int GetParameters(long handle, int parameter, byte[] pValue, int[] size);
    public static native int AcquireFingerprint(long handle, byte[] fpImage, byte[] fpTemplate, int[] size);
    public static native long DBInit();
    public static native void DBFree(long handle);
    public static native int DBMatch(long handle, byte[] template1, byte[] template2);
} 