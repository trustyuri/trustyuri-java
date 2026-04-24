package net.trustyuri;

import java.io.File;
import java.io.IOException;

/**
 * This class can be used to fix a trusty file.
 *
 * @author Tobias Kuhn
 */
public class FixTrustyFile {

    /**
     * Fixes the given trusty file(s).
     *
     * @param args the trusty file(s) to fix
     * @throws IOException        if there is an error reading or writing the file
     * @throws TrustyUriException if there is an error with the trusty URI, for example if the file is not a trusty file or if the module is unknown
     */
    public static void main(String[] args) throws IOException, TrustyUriException {
        for (String arg : args) {
            fix(new File(arg));
        }
    }

    /**
     * Fixes the given trusty file.
     *
     * @param file the trusty file to fix
     * @throws IOException        if there is an error reading or writing the file
     * @throws TrustyUriException if there is an error with the trusty URI, for example if the file is not a trusty file or if the module is unknown
     */
    public static void fix(File file) throws IOException, TrustyUriException {
        FixTrustyFile c = new FixTrustyFile(file);
        c.fix();
    }

    private File file;

    /**
     * Creates a new FixTrustyFile object for the given trusty file.
     *
     * @param file the trusty file to fix
     */
    public FixTrustyFile(File file) {
        this.file = file;
    }

    /**
     * Fixes the trusty file. The file is modified in place, so the original file is overwritten.
     *
     * @throws IOException        if there is an error reading or writing the file
     * @throws TrustyUriException if there is an error with the trusty URI, for example if the file is not a trusty file or if the module is unknown
     */
    public void fix() throws IOException, TrustyUriException {
        TrustyUriResource r = new TrustyUriResource(file);
        TrustyUriModule module = ModuleDirectory.getModule(r.getModuleId());
        if (module == null) {
            throw new TrustyUriException("ERROR: Not a trusty URI or unknown module");
        }
        module.fixTrustyFile(file);
    }

}
