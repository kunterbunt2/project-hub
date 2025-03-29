package de.bushnaq.abdalla.projecthub.dao;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class Context {
    //    public Requests active = new Requests();
//    public List<AbstractDevelopmentRequest> activeList = null;
//    public Authentication authentication = new Authentication();
//    public Map<LocalDate, String> bankHolidays;
//    public List<AbstractDevelopmentRequest> closedList = null;
//    public List<AbstractDevelopmentRequest> closingList = null;
//    public Debug debug = new Debug();
//    public GanttInformationList ganttInformationList = new GanttInformationList();
//    public Index index = new Index();
//    private JiraClientFactory jiraClientFactory;
//    public Logs logs = new Logs();
//    public ArrayList<JiraSprint> orphanSprintList = new ArrayList<>();
    public ParameterOptions parameters = new ProjectsDashboardParameterOptions();

    //    public ResourceUtilization resourceUtilization = new ResourceUtilization();
//    public List<SfpsTicket> spfsList;
//    private Map<Integer, Integer> taskIdToRowIndexMap = new HashMap<>();
//    @Autowired
//    public TimeTracker timeTracker;
//
//    public void addTaskIdToRowIndexRelation(Task task, Integer rowId) {
//        taskIdToRowIndexMap.put(task.getID(), rowId);
//    }
//
//    public JiraClientFactory getJiraClientFactory() {
//        return jiraClientFactory;
//    }
//
//    public Integer getRowIndexByTaskId(Task task) {
//        Integer rowIndex = taskIdToRowIndexMap.get(task.getID());
//        if (rowIndex != null) {
//            return ExcelUtil.rowIndextoExcelRowName(rowIndex);
//        } else {
//            return task.getID();
//        }
//    }
//
//    public Integer getRowIndexByTaskIdIfExists(Task task) {
//        Integer rowIndex = taskIdToRowIndexMap.get(task.getID());
//        return rowIndex;
//    }
//
//    public void init(ParameterOptions parameters) {
//        this.parameters = parameters;
//    }
//
//    public void setJiraClientFactory(JiraClientFactory jiraClientFactory) {
//        this.jiraClientFactory = jiraClientFactory;
//    }
//
//    public void start() {
//        jiraClientFactory = new JiraClientProductionFactory(parameters.reportFolder);
//    }
//
//    public void start(String[] args) throws Exception {
//        parameters.start(args);
//        jiraClientFactory = new JiraClientProductionFactory(parameters.reportFolder);
//    }
}
