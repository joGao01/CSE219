package dataprocessors;

public class Configuration {

    private int maxIterations;
    private int updateIntervals;
    private boolean continuousRun;
    private int numberOfClusters;

    public Configuration(int mIterations, int uIntervals, boolean cRun){
        maxIterations = mIterations;
        updateIntervals = uIntervals;
        continuousRun = cRun;
    }

    public Configuration(int mIterations, int uIntervals, boolean cRun, int numClusters){
        maxIterations = mIterations;
        updateIntervals = uIntervals;
        continuousRun = cRun;
        numberOfClusters = numClusters;
    }

    public int getMaxIterations(){ return maxIterations; }

    public int getUpdateIntervals(){ return updateIntervals; }

    public boolean getContinuousRun() {return continuousRun; }

    public int getNumberOfClusters() { return numberOfClusters; }

    public void setMaxIterations(int mIterations) { maxIterations = mIterations; }
    public void setUpdateIntervals(int uIntervals) { updateIntervals = uIntervals; }
    public void setContinuousRun(boolean cRun) { continuousRun = cRun; }
    public void setNumberOfClusters(int nClusters) { numberOfClusters = nClusters; }

    public String toString(){
        return "Max Iterations: " + maxIterations +
                ", Update Intervals: " + updateIntervals +
                ", Continuous Run: " + continuousRun;
    }

}
