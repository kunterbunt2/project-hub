package de.bushnaq.abdalla.projecthub.dao;

import de.bushnaq.abdalla.projecthub.report.dao.BurnDownGraphicsTheme;
import de.bushnaq.abdalla.projecthub.report.dao.GraphicsLightTheme;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


public abstract class ParameterOptions {
    protected static final String                CLI_OPTION_CAPTURE_COST                     = "capturecost";
    protected static final String                CLI_OPTION_CAPTURE_OUT_OF_OFFICE_FROM_GANTT = "captureoutofofficefromgantt";
    protected static final String                CLI_OPTION_CAPTURE_OUT_OF_OFFICE_FROM_RAM   = "captureoutofofficefromram";
    protected static final String                CLI_OPTION_DATE                             = "date";
    protected static final String                CLI_OPTION_DETAILED                         = "detailed";
    protected static final String                CLI_OPTION_DISABLE_PROXY                    = "disableproxy";
    protected static final String                CLI_OPTION_ENV                              = "env";
    protected static final String                CLI_OPTION_INDIVIDUAL_LOG                   = "individuallog";
    protected static final String                CLI_OPTION_QUERY_TEAM_PLANNER               = "queryteamplanner";
    protected static final String                CLI_OPTION_QUICK_MODE                       = "quick";
    protected static final String                CLI_OPTION_REPORT_FOLDER                    = "reportfolder";
    protected static final String                CLI_OPTION_RESOURCE_MAP                     = "resourcemap";
    protected static final String                CLI_OPTION_THEME                            = "theme";
    protected static final String                CLI_OPTION_VERBOSE                          = "verbose";
    protected static final String                CLI_OPTION_XLSX_FILE                        = "xlsxfile";
    public                 boolean               activeRequests                              = true;//for test purposes
    public                 boolean               captureCost                                 = false;//for cost charts
    public                 boolean               captureOutOfOfficeFromGantt                 = false;//get out of office information from Gantt charts
    public                 boolean               captureOutOfOfficeFromRam                   = false;//get out of office information from RAM db
    public                 boolean               closedRequests                              = true;
    public                 boolean               closingRequests                             = true;
    public                 boolean               detailed                                    = false;//should always be true, otherwise resources will not be shown in burn down chart
    //    public String domainName = "DOMAIN";
    public                 List<Throwable>       exceptions                                  = new ArrayList<>();
    public                 String[]              files                                       = {};
    //    public String folder = null;//for test purposes
    public                 BurnDownGraphicsTheme graphicsTheme                               = new GraphicsLightTheme();
    public                 boolean               individualLog                               = false;//resource individual log visible as drill down list of work burn down chart. Legacy!
    // public boolean infringements = false;
    public                 Integer               limitProjectOverview                        = null;// 6 * 30;
    public                 Integer               limitResourceUtilization                    = null;// 1 * 30;
    protected final        Logger                logger                                      = LoggerFactory.getLogger(this.getClass());
    public static          LocalDateTime         now                                         = LocalDateTime.now();
    public                 boolean               outOfOfficeOnly                             = false;//no ramdb cost records
    //    public String password = "PManager2018";
    public                 boolean               queryTeamPlanner;
    public                 boolean               quickMode                                   = false;//only query active projects and resource map
    public                 String                reportFolder                                = "./";
    public                 boolean               resourceMap                                 = false;
    public                 boolean               resourceUtilizationPane                     = false;//only used in tests to cover code, currently cannot be enabled in production mode
    //    public long smbTimeout = 180;
    //    public boolean useLocalShareFolder = false;
//    public SmbParameters smbParameters = new SmbParameters();
    public                 boolean               verbose                                     = false;//in verbose mode, temporary <filename>-tp.xml file will not be deleted.
    public                 String                xlsxFile                                    = null;//used only by Xlsx2mppMain

    public abstract void start(String[] args) throws Exception;

}
