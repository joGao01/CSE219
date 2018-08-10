package Clustering;
import algorithms.Clusterer;
import dataprocessors.AppData;
import dataprocessors.Configuration;
import dataprocessors.DataSet;
import dataprocessors.Drop;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import ui.AppUI;
import vilij.templates.ApplicationTemplate;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Ritwik Banerjee
 */
public class KMeansClusterer extends Clusterer {
    private DataSet dataset;
    private List<Point2D> centroids;

    private final int           maxIterations;
    private final int           updateInterval;
    private final AtomicBoolean tocontinue;
    private boolean             actualToContinue;
    private ApplicationTemplate applicationTemplate;
    private Drop drop;
    private Configuration config;
    private boolean isLast;

    public KMeansClusterer(DataSet dataset, Configuration config, ApplicationTemplate applicationTemplate, Drop drop) {
        super(config.getNumberOfClusters());
        this.dataset = dataset;
        this.maxIterations = config.getMaxIterations();
        this.updateInterval = config.getUpdateIntervals();
        this.tocontinue = new AtomicBoolean(false);
        this.actualToContinue = config.getContinuousRun();
        this.applicationTemplate = applicationTemplate;
        this.drop = drop;
        this.config = config;
    }

    @Override
    public int getMaxIterations() { return maxIterations; }

    @Override
    public int getUpdateInterval() { return updateInterval; }

    @Override
    public boolean tocontinue() { return actualToContinue; }

    @Override
    public synchronized void run() {
        ReentrantLock lock = new ReentrantLock();
        initializeCentroids();
        int iteration = 0; //actualToContinue is not updating with the config
        System.out.println(config);
        actualToContinue = config.getContinuousRun();

        isLast = false;
        if(actualToContinue) {
            lock.lock();
            while ((iteration++ < maxIterations) && tocontinue.get()) {
                AppData appData = ((AppData)applicationTemplate.getDataComponent());
                assignLabels();
                recomputeCentroids();

                if(iteration % updateInterval == 0 || iteration == maxIterations || isLast) {
                    Platform.runLater(() -> appData.displayClusterData(dataset));
                }
                System.out.println(iteration);
                try{
                    Thread.sleep(1000);
                } catch (InterruptedException e){
                    //something
                }
            }
            ((AppUI)applicationTemplate.getUIComponent()).setScrnshotButtonDisable(false);
            ((AppUI)applicationTemplate.getUIComponent()).getRunButton().setDisable(false);
            lock.unlock();
            tocontinue.set(true);

        } else {
            tocontinue.set(true);//toContinue is being set to true ans triggering the wrong thing in the appData
            while (iteration++ < maxIterations && tocontinue.get()) {
                System.out.println("In Loop");
                AppData appData = ((AppData)applicationTemplate.getDataComponent());
                assignLabels();
                recomputeCentroids();

                System.out.println(iteration);
                if(iteration % updateInterval == 0 || iteration == maxIterations) {
                    System.out.println("Iteration: " + iteration);
                    Platform.runLater(() -> appData.displayClusterData(dataset));
                    drop.take();
                }

             //   if(iteration >= maxIterations) tocontinue.set(false);
             //   tocontinue.set(false);
            }

        }

    }

    private void initializeCentroids() {
        Set<String>  chosen        = new HashSet<>();
        List<String> instanceNames = new ArrayList<>(dataset.getLabels().keySet());
        Random       r             = new Random();
        while (chosen.size() < numberOfClusters) {
            int i = r.nextInt(instanceNames.size());
            while (chosen.contains(instanceNames.get(i)))
                i = (++i % instanceNames.size());
            chosen.add(instanceNames.get(i));
        }
        centroids = chosen.stream().map(name -> dataset.getLocations().get(name)).collect(Collectors.toList());
        tocontinue.set(true);
    }

    private void assignLabels() {
        dataset.getLocations().forEach((instanceName, location) -> {
            double minDistance      = Double.MAX_VALUE;
            int    minDistanceIndex = -1;
            for (int i = 0; i < centroids.size(); i++) {
                double distance = computeDistance(centroids.get(i), location);
                if (distance < minDistance) {
                    minDistance = distance;
                    minDistanceIndex = i;
                }
            }
            dataset.getLabels().put(instanceName, Integer.toString(minDistanceIndex));
        });
    }

    private void recomputeCentroids() {
        tocontinue.set(false);
        IntStream.range(0, numberOfClusters).forEach(i -> {
            AtomicInteger clusterSize = new AtomicInteger();
            Point2D sum = dataset.getLabels()
                    .entrySet()
                    .stream()
                    .filter(entry -> i == Integer.parseInt(entry.getValue()))
                    .map(entry -> dataset.getLocations().get(entry.getKey()))
                    .reduce(new Point2D(0, 0), (p, q) -> {
                        clusterSize.incrementAndGet();
                        return new Point2D(p.getX() + q.getX(), p.getY() + q.getY());
                    });
            Point2D newCentroid = new Point2D(sum.getX() / clusterSize.get(), sum.getY() / clusterSize.get());
            if (!newCentroid.equals(centroids.get(i))) {
                centroids.set(i, newCentroid);
                tocontinue.set(true);
            }
        });
        if(!tocontinue()){
            System.out.println("isLast");
            isLast = true;
        }
    }

    private static double computeDistance(Point2D p, Point2D q) {
        return Math.sqrt(Math.pow(p.getX() - q.getX(), 2) + Math.pow(p.getY() - q.getY(), 2));
    }

}
