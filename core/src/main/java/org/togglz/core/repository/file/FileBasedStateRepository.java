package org.togglz.core.repository.file;

import java.io.File;

import org.togglz.core.logging.Log;
import org.togglz.core.logging.LogFactory;
import org.togglz.core.repository.property.PropertyBasedStateRepository;

/**
 * 
 * <p>
 * A {@link PropertyBasedStateRepository} that stores the state of features using a standard Java properties file.
 * This class is able to detect changes made to the properties file and will automatically reload it in this
 * case.
 * </p>
 * 
 * @author Christian Kaltepoth
 * 
 */
public class FileBasedStateRepository extends PropertyBasedStateRepository {

    private static final Log log = LogFactory.getLog(FileBasedStateRepository.class);

    /**
     * Constructor for {@link FileBasedStateRepository}.
     * 
     * @param file A {@link File} representing the Java properties file to use.
     */
    public FileBasedStateRepository(File file) {
        this(file, 1000);
    }

    /**
     * Constructor for {@link FileBasedStateRepository}.
     * 
     * @param file A {@link File} representing the Java properties file to use.
     * @param minCheckInterval the minimum amount of time in milliseconds to wait between checks of the file's modification
     *        date.
     */
    public FileBasedStateRepository(File file, int minCheckInterval) {
        super(new ReloadablePropertiesFile(file, minCheckInterval));
        log.debug(this.getClass().getSimpleName() + " initialized with: " + file.getAbsolutePath());
    }

}
