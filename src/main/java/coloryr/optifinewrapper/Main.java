package coloryr.optifinewrapper;

import optifine.*;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) throws Exception {
        String libDir = System.getProperty("libdir");
        String mcJar = System.getProperty("gamecore");
        String optifine = System.getProperty("optifine");

        String[] lib;
        boolean old = false;
        Method add1;
        Object loader1;

        ClassLoader loader = ClassLoader.getSystemClassLoader();

        if (loader instanceof URLClassLoader) {
            loader1 = Main.class.getClassLoader();
            add1 = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            add1.setAccessible(true);
            add1.invoke(loader1, new File(optifine).toURI().toURL());
            old = true;
        } else {
            Class<?> class1 = Class.forName("jdk.internal.loader.BuiltinClassLoader");
            Field field = class1.getDeclaredField("ucp");
            field.setAccessible(true);
            Class<?> class2 = Class.forName("jdk.internal.loader.URLClassPath");
            add1 = class2.getDeclaredMethod("addFile", String.class);
            loader1 = field.get(loader);
            add1.invoke(loader1, optifine);
        }

        try (URLClassLoader ucl = URLClassLoader.newInstance(new URL[]{
                Main.class.getProtectionDomain().getCodeSource().getLocation()
        }, ClassLoader.getSystemClassLoader())) {
            Class<?> installer = ucl.loadClass("coloryr.optifinewrapper.Main");
            if (!((boolean) installer.getMethod("getInfo", String.class).invoke(null, libDir))) {
                installer.getMethod("startInstall", String.class, String.class)
                        .invoke(null, libDir, mcJar);
            }

            lib = (String[]) installer.getMethod("getLib", String.class)
                    .invoke(null, libDir);
        }

        for (String s : lib) {
            System.out.println("addlib: " + s);
            if(old) {
                add1.invoke(loader1, new File(s).toURI().toURL());
            }
            else {
                add1.invoke(loader1, s);
            }
        }

        Class<?> mainClass = loader.loadClass("net.minecraft.launchwrapper.Launch");
        mainClass.getMethod("main", String[].class).invoke(null, new Object[]{args});
    }

    public static boolean getInfo(String libDir) throws Exception{
        String ofVer = Installer.getOptiFineVersion();

        String[] ofVers = Utils.tokenize(ofVer, "_");
        String mcVer = ofVers[1];
        String ofEd = Installer.getOptiFineEdition(ofVers);
        File dirDest = new File(libDir, "optifine/OptiFine/" + mcVer + "_" + ofEd);
        File fileDest = new File(dirDest, "OptiFine-" + mcVer + "_" + ofEd + ".jar");

        return fileDest.exists();
    }

    public static String[] getLib(String libDir) throws Exception{
        String ofVer = Installer.getOptiFineVersion();

        String[] ofVers = Utils.tokenize(ofVer, "_");
        String mcVer = ofVers[1];
        String ofEd = Installer.getOptiFineEdition(ofVers);
        File dirDest = new File(libDir, "optifine/OptiFine/" + mcVer + "_" + ofEd);
        File fileDest = new File(dirDest, "OptiFine-" + mcVer + "_" + ofEd + ".jar");

        Class<?> cls = Class.forName("optifine.Installer");

        Method method1 = cls.getDeclaredMethod("getLaunchwrapperVersion");
        method1.setAccessible(true);
        String ver = (String) method1.invoke(null);

        String fileName = "launchwrapper-of-" + ver + ".jar";
        File dirDest1 = new File(libDir, "optifine/launchwrapper-of/" + ver);
        File fileDest1 = new File(dirDest1, fileName);

        return new String[] { fileDest.getAbsolutePath(), fileDest1.getAbsolutePath() };
    }

    public static void startInstall(String libDir, String mc) throws Exception{
        String ofVer = Installer.getOptiFineVersion();

        String[] ofVers = Utils.tokenize(ofVer, "_");
        String mcVer = ofVers[1];
        String ofEd = Installer.getOptiFineEdition(ofVers);
        String mcVerOf = mcVer + "-OptiFine_" + ofEd;

        Class cls = Class.forName("optifine.Installer");

//        Installer.copyMinecraftVersion(mcVer, mcVerOf, dirMcVers);

        System.out.println("OptiFine Version: " + ofVer);
        System.out.println("OptiFine Version: " + mcVer);
        System.out.println("Minecraft_OptiFine Version: " + mcVerOf);

        //        Installer.installOptiFineLibrary(mcVer, ofEd, dirMcLib, false);
        {
            File fileSrc = Installer.getOptiFineZipFile();
            File dirDest = new File(libDir, "optifine/OptiFine/" + mcVer + "_" + ofEd);
            File fileDest = new File(dirDest, "OptiFine-" + mcVer + "_" + ofEd + ".jar");
            if (fileDest.getParentFile() != null) {
                fileDest.getParentFile().mkdirs();
            }

            Patcher.process(new File(mc), fileSrc, fileDest);
        }

//        Installer.installLaunchwrapperLibrary(mcVer, ofEd, dirMcLib);

//       String ver =  Installer.getLaunchwrapperVersion();

        Method method1 = cls.getDeclaredMethod("getLaunchwrapperVersion");
        method1.setAccessible(true);
        String ver = (String) method1.invoke(null);

        String fileName = "launchwrapper-of-" + ver + ".jar";
        File dirDest = new File(libDir, "optifine/launchwrapper-of/" + ver);
        File fileDest = new File(dirDest, fileName);

        InputStream fin = Installer.class.getResourceAsStream("/" + fileName);
        if (fin == null) {
            throw new IOException("File not found: " + fileName);
        }
        if (fileDest.getParentFile() != null) {
            fileDest.getParentFile().mkdirs();
        }
        FileOutputStream fout = new FileOutputStream(fileDest);
        Utils.copyAll(fin, fout);
        fout.flush();
        fin.close();
        fout.close();
    }
}