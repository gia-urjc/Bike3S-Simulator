import { JsonObject } from "../../../shared/util";
import { Station, User, Entity } from "../systemDataTypes/Entities";
import { SystemGlobalInfo } from "../analyzers/metrics/SystemGlobalInfo";
import { AbsoluteValue } from "../analyzers/AbsoluteValue";
import { SystemInfo } from "../analyzers/SystemInfo";
import { StationBalancingQuality, StationBalancingData } from '../analyzers/metrics/stations/StationBalancingQuality';
import { BikesPerTime, BikesPerStationAndTime } from '../analyzers/metrics/stations/BikesPerStationAndTime';
import { RentalAndReturnAbsoluteValue } from "../analyzers/metrics/rentalsAndReturns/RentalAndReturnAbsoluteValue";
import { RentalAndReturnData } from "../analyzers/metrics/rentalsAndReturns/RentalAndReturnData";
import { RentalsAndReturnsPerStation } from "../analyzers/metrics/rentalsAndReturns/RentalsAndReturnsPerStation";
import { RentalsAndReturnsPerUser } from "../analyzers/metrics/rentalsAndReturns/RentalsAndReturnsPerUser";
import { ReservationAbsoluteValue } from '../analyzers/metrics/reservations/ReservationAbsoluteValue';
import { ReservationDaserta, ReservationData } from "../analyzers/metrics/reservations/ReservationData";

import { ReservationsPerStation } from "../analyzers/metrics/reservations/ReservationsPerStation";
import { ReservationsPerUser } from "../analyzers/metrics/reservations/ReservationsPerUser";
import { EmptyStationInfo, EmptyStateData } from "../analyzers/metrics/stations/EmptyStationInfo";
import { UserTimeAtSystem, UserTimeData, UserTimeAbsoluteValue } from '../analyzers/metrics/users/UserTimeAtSystem';
import * as json2csv from 'json2csv';
import * as fs from 'fs';

export class CsvGenerator {
    
	private readonly NUM_DATA: number = 4;   // number of absolute values of reservation and rental and return data  
    private csvPath: string;
    
    private stationInfoTitles: Array<string>;
    private userInfoTitles: Array<string>;
    private globalInfoTitles: Array<string>;
    private emptyStationTitles: Array<string>;
    private bikesBalanceTitles: Array<string>;
    private bikesPerStationTitles: Array<string>;
    
   	private stationData: Array<JsonObject>;
   	private userData: Array<JsonObject>;
    private globalInfo: JsonObject;
    private emptyStationData: Array<JsonObject>;
    private bikesBalanceData: Array<JsonObject>;
    private bikesPerStationData: Array<JsonObject>;
    
    public constructor(csvPath: string) {
        this.csvPath = csvPath;
        this.stationInfoTitles = new Array();
        this.userInfoTitles = new Array();
        this.globalInfoTitles = new Array();
        this.emptyStationTitles = new Array();
        this.bikesBalanceTitles = new Array();
        this.bikesPerStationTitles = new Array();
        this.stationData = new Array();
        this.userData = new Array();
        this.globalInfo = {};
        this.emptyStationData = new Array();
        this.bikesBalanceData = new Array();
        this.bikesPerStationData = new Array();
  }
    
  public createJsonForStations(stations: Array<Entity>, reservations: SystemInfo, rentalsAndReturns: SystemInfo): void {
    let i: number = 1;  // title index
    let j: number = 0;  // data index
      
    for (let station of stations) {
        let jsonObj: JsonObject = {};
        
        jsonObj.id = station.id;
        
        while (j < ReservationAbsoluteValue.NUM_ATTR) {   // adding reservations' data
            let reservationValues: AbsoluteValue | undefined = reservations.getData().absoluteValues.get(station.id);
            if (reservationValues !== undefined) {
                let absValueList: Array<number> = reservationValues.getAbsoluteValuesAsArray();
                jsonObj[this.stationInfoTitles[i]] = absValueList[j]; 
                i++;
                j++;
            }
        }
        
        j = 0;
        while (j < RentalAndReturnAbsoluteValue.NUM_ATTR) {   // adding rentals and returns' data
            let rentalAndReturnValues: AbsoluteValue | undefined = rentalsAndReturns.getData().absoluteValues.get(station.id);
            if (rentalAndReturnValues !== undefined) {
                let absValueList: Array<number> = rentalAndReturnValues.getAbsoluteValuesAsArray();
                jsonObj[this.stationInfoTitles[i]] = absValueList[j];
                i++;   
                j++;
            }
        }
        this.stationData.push(jsonObj);
        i = 1;
        j = 0;
    }
  }

    private async initStationInfoTitles(): Promise<void> {
         this.stationInfoTitles.push('id');
         ReservationData.NAMES.forEach( (name) => this.stationInfoTitles.push(name));
         RentalAndReturnData.NAMES.forEach( (name) => this.stationInfoTitles.push(name));
         return;
    }
     
     private async initStationInfo(info: Map<string, SystemInfo>, stations: Array<Station>): Promise<void> {
         let reservations: SystemInfo | undefined = info.get(ReservationsPerStation.name);
         let rentalsAndReturns: SystemInfo | undefined = info.get(RentalsAndReturnsPerStation.name);
         if (reservations && rentalsAndReturns) {
           this.createJsonForStations(stations, reservations, rentalsAndReturns);
         }
         return;
     }
    
  private createJsonForUsers(users: Array<Entity>, timeIntervals: SystemInfo, reservations: SystemInfo, rentalsAndReturns: SystemInfo): void {
    let i: number = 1;  // title index
    let j: number = 0;  // data index
      
    for (let user of users) {
        let jsonObj: JsonObject = {};
        
        jsonObj.id = user.id;
        
        while (j < UserTimeAbsoluteValue.NUM_ATTR) {
            let timeValues: AbsoluteValue | undefined = timeIntervals.getData().absoluteValues.get(user.id);
            if (timeValues !== undefined) {
                let absValueList: Array<number> = timeValues.getAbsoluteValuesAsArray();
                if (absValueList[j] !== 0) {
                    jsonObj[this.userInfoTitles[i]] = absValueList[j];
                }
                else {
                    jsonObj[this.userInfoTitles[i]] = "";
                }
                i++;
                j++;
            }
        }
        j=0;
        
        while (j < ReservationAbsoluteValue.NUM_ATTR) {   // adding reservations' data
            let reservationValues: AbsoluteValue | undefined = reservations.getData().absoluteValues.get(user.id);
            if (reservationValues !== undefined) {
                let absValueList: Array<number> = reservationValues.getAbsoluteValuesAsArray();
                jsonObj[this.userInfoTitles[i]] = absValueList[j]; 
                i++;
                j++;
            }
        }
        
        j = 0;
        while (j < RentalAndReturnAbsoluteValue.NUM_ATTR) {   // adding rentals and returns' data
            let rentalAndReturnValues: AbsoluteValue | undefined = rentalsAndReturns.getData().absoluteValues.get(user.id);
            if (rentalAndReturnValues !== undefined) {
                let absValueList: Array<number> = rentalAndReturnValues.getAbsoluteValuesAsArray();
                jsonObj[this.userInfoTitles[i]] = absValueList[j];
                i++;   
                j++;
            }
        }
        this.userData.push(jsonObj);
        i = 1;
        j = 0;
    }
  }
      
  private async initUserInfoTitles(): Promise<void> {
      this.userInfoTitles.push('id');
      UserTimeData.NAMES.forEach( (name) => this.userInfoTitles.push(name));
      ReservationData.NAMES.forEach( (name) => this.userInfoTitles.push(name));
      RentalAndReturnData.NAMES.forEach( (name) => this.userInfoTitles.push(name));
      return;
  }
     
    private async initUserInfo(info: Map<string, SystemInfo>, users: Array<User>): Promise<void> {
        let timeIntervals: UserTimeAtSystem | undefined = info.get(UserTimeAtSystem.name);
         let reservations: SystemInfo | undefined = info.get(ReservationsPerUser.name);
         let rentalsAndReturns: SystemInfo | undefined = info.get(RentalsAndReturnsPerUser.name);
         if (timeIntervals && reservations && rentalsAndReturns) {   
           this.createJsonForUsers(users, timeIntervals, reservations, rentalsAndReturns);
         }
        return;
    }
    
    private async initGlobalInfo(globalInfo: SystemGlobalInfo): Promise<void> {
        SystemGlobalInfo.NAMES.forEach( (name) => this.globalInfoTitles.push(name));
        
        let data: Array<number> = globalInfo.getDataAsArray();
        for(let i = 0; i<data.length; i++) {
            this.globalInfo[this.globalInfoTitles[i]] = data[i];
        }
        return;
    }
    
    private async initEmptyStationInfo(info: Map<string, SystemInfo>): Promise<void> {
        this.emptyStationTitles[0] = 'id';
        let emptyStations: SystemInfo | undefined = info.get(EmptyStationInfo.name);
        if (emptyStations) {
             EmptyStateData.NAMES.forEach( (name) => {
             this.emptyStationTitles.push(name);
            });
            emptyStations.getData().absoluteValues.forEach( (v, k) => {
                let jsonObj: JsonObject = {};
                jsonObj.id = k;
                jsonObj[this.emptyStationTitles[0]] = v.intervalsToString();
                jsonObj[this.emptyStationTitles[1]] = v.totalTime;
                this.emptyStationData.push(jsonObj);
            });
        }
        return;
    }
    
    private async initBikesBalancingInfo(info: Map<string, SystemInfo>): Promise<void> {
        this.bikesBalanceTitles[0] = 'id';
        this.bikesBalanceTitles[1] = StationBalancingData.NAMES;
        let bikesBalance: SystemInfo | undefined = info.get(StationBalancingQuality.name); 
        if (bikesBalance) {
            bikesBalance.getData().absoluteValues.forEach( (value, stationId) => {
                let obj: JsonObject = {};
                obj[this.bikesBalanceTitles[0]] = stationId;
                obj[this.bikesBalanceTitles[1]] = value.quality;
                this.bikesBalanceData.push(obj);
            });
        }
        return;
    }
    private async initBikesPerStationInfo(bikesPerStation: BikesPerStationAndTime): Promise<void> {
        this.bikesPerStationTitles[0] = 'id';
        this.bikesPerStationTitles[1] = 'time';
        this.bikesPerStationTitles[2] = 'available bikes';
        
        bikesPerStation.getStations().forEach( (bikesList, stationId) => {
            let list: Array<BikesPerTime> = bikesList.getList();
            for (let item of list) {
                let obj: JsonObject = {};
                obj[this.bikesPerStationTitles[0]] = stationId;
                obj[this.bikesPerStationTitles[1]] = item.time;
                obj[this.bikesPerStationTitles[2]] = item.availableBikes;
                this.bikesPerStationData.push(obj);
            }
            });
        return;
    } 

	   public async generate(entityInfo: Map<string, SystemInfo>, globalInfo: SystemGlobalInfo, bikesPerStation: BikesPerStationAndTime, stations: Array<Station>, users: Array<User>): Promise<void> {
         await this.initStationInfoTitles();
           
         await this.initStationInfo(entityInfo, stations);
         this.transformStationJsonToCsv();
           
         await this.initUserInfoTitles();
         await this.initUserInfo(entityInfo, users);
         this.transformUserJsonToCsv();
           
         await this.initGlobalInfo(globalInfo); 
         this.transformGlobalInfoJsonToCsv();
           
         await this.initEmptyStationInfo(entityInfo);
         this.transformEmptyStationJsonToCsv();
        
         await this.initBikesBalancingInfo(entityInfo);
         this.transformBikesBalanceJsonToCsv();
        
         await this.initBikesPerStationInfo(bikesPerStation);
         this.transformBikesPerStationJsonToCsv();
        return;
   	}

	private transformStationJsonToCsv(): void {
        let csv = json2csv({ data: this.stationData, fields: this.stationInfoTitles, withBOM: true, del: ';' });
        this.checkFolders();
        fs.writeFile (`${this.csvPath}/stations.csv`, csv, (err) => {
          if (err) throw err;
          console.log('stations file saved');
        });
	}
    
    private transformUserJsonToCsv(): void {
        let csv = json2csv({ data: this.userData, fields: this.userInfoTitles, withBOM: true, del: ';' });
        this.checkFolders();
        fs.writeFile (`${this.csvPath}/users.csv`, csv, (err) => {
          if (err) throw err;
          console.log('user file saved');
        });
    }

    private transformGlobalInfoJsonToCsv(): void {
        let csv = json2csv({data: [this.globalInfo], fields: this.globalInfoTitles, withBOM: true, del: ';' });
        this.checkFolders();
        fs.writeFile(`${this.csvPath}/global_values.csv`, csv, (err) => {
            if (err) throw err;
            console.log('global values file saved');
        });
    }
    
    private transformEmptyStationJsonToCsv(): void {
        let csv = json2csv({data: this.emptyStationData, fields: this.emptyStationTitles, withBOM: true, del: ';' });
        this.checkFolders();
        fs.writeFile(`${this.csvPath}/empty_stations.csv`, csv, (err) => {
            if (err) throw err;
            console.log('empty stations file saved');
        });        
    }

	private checkFolders(): void {
        if(!fs.existsSync(this.csvPath)) {
            fs.mkdirSync(this.csvPath);
        }
    }
    
    private transformBikesBalanceJsonToCsv(): void {
        let csv = json2csv({ data: this.bikesBalanceData, fields: this.bikesBalanceTitles, withBOM: true, del: ';' });
        this.checkFolders();
        fs.writeFile (`${this.csvPath}/stationBalancingQuality.csv`, csv, (err) => {
          if (err) throw err;
          console.log('Bikes balance quality file saved');
        });
    }
    
    private transformBikesPerStationJsonToCsv(): void {
        let csv = json2csv({ data: this.bikesPerStationData, fields: this.bikesPerStationTitles, withBOM: true, del: ';' });
        this.checkFolders();
        fs.writeFile (`${this.csvPath}/bikesPerStationAndTime.csv`, csv, (err) => {
          if (err) throw err;
          console.log('Bikes per station and time file saved');
        });
    }
    
    
}  