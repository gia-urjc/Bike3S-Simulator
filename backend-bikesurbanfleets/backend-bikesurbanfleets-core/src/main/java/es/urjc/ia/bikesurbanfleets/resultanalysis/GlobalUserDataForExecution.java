/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.urjc.ia.bikesurbanfleets.resultanalysis;

import java.util.TreeMap;

/**
 *
 * @author holger
 */
class GlobalUserDataForExecution {
        double avtostationtime = 0;
        int totalusers = 0;
        int finishedusers=0;
        double avbetweenstationtime = 0;
        double avfromstationtime = 0;
        int avabandonos = 0;
        double DS=0.0D;
        double HE=0.0D;
        double RE=0.0D;
        TreeMap<Integer, Integer> usertakefails = new TreeMap<>();
        TreeMap<Integer, Integer> userreturnfails = new TreeMap<>();
        double avtimeloss=0;
        int totalfailedrentals=0;
        int totalfailedreturns=0;
}
