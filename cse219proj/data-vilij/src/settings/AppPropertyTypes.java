package settings;

/**
 * This enumerable type lists the various application-specific property types listed in the initial set of properties to
 * be loaded from the workspace properties <code>xml</code> file specified by the initialization parameters.
 *
 * @author Ritwik Banerjee
 * @see vilij.settings.InitializationParams
 */
public enum AppPropertyTypes {

    /* resource files and folders */
    DATA_RESOURCE_PATH,
    STYLESHEET_PATH,

    /* user interface icon file names */
    SCREENSHOT_ICON,
    CONFIG_ICON,
    RUN_ICON,
    PAUSE_ICON,

    /* tooltips for user interface buttons */
    SCREENSHOT_TOOLTIP,
    CREATE_TOOLTIP,
    CONFIG_TOOLTIP,

    /*warnings*/
    LOAD_ALERT_TITLE,
    LOAD_ALERT_MSG1,
    LOAD_ALERT_MSG2,

    /* error messages */
    RESOURCE_SUBDIR_NOT_FOUND,
    SAVE_ERROR_TITLE,
    SAVE_ERROR_MSG,
    FORMAT_ERROR_TITLE,
    FORMAT_ERROR_MSG,
    DUPLICATE_ERROR_TITLE,
    DUPLICATE_ERROR_MSG,
    ERROR_MSG,
    ERROR_TITLE,
    NO_DATA_TITLE,
    NO_DATA_MSG,

    /* application-specific message titles */
    SAVE_UNSAVED_WORK_TITLE,

    /* application-specific messages */
    SAVE_UNSAVED_WORK,
    THERE_ARE,

    /* application-specific parameters */
    DATA_FILE_EXT,
    DATA_FILE_EXT_DESC,
    SCRNSHOT_EXT_TO_SHOW,
    SCRNSHOT_EXT,
    SCRNSHOT_EXT_DESC,
    TEXT_AREA,
    SPECIFIED_FILE,
    INITIAL_FILE_NAME,

    /*LABELS*/
    CHART_LABEL,
    EDIT_BUTTON_LABEL1,
    EDIT_BUTTON_LABEL2,
    ALGORITHM1,
    ALGORITHM2,
    ALGORITHM3,
    RUN_LABEL,
    PAUSE_LABEL,

    /*ALGORITHMS*/
    CLASSIFICATION_LABEL,
    CLUSTERING_LABEL,

    /*CSS IDs*/
    TEXTAREA_ID,
    CHART_ID,
    TEXTAREA2_ID,

    CHART_CSS,
    CHART_FIRST_LINE
}
