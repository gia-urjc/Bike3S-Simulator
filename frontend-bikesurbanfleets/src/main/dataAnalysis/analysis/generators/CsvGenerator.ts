import { HistoryEntitiesJson } from "../../../../shared/history";
import { JsonObject } from "../../../../shared/util";
import { HistoryReader } from "../../../util";
import { Station, User } from "../../systemDataTypes/Entities";
import { RentalsAndReturnsPerStation } from "../absoluteValues/rentalsAndReturns/RentalsAndReturnsPerStation";
import { RentalsAndReturnsPerUser } from "../absoluteValues/rentalsAndReturns/RentalsAndReturnsPerUser";
import { ReservationsPerStation } from "../absoluteValues/reservations/ReservationsPerStation";
import { ReservationsPerUser } from "../absoluteValues/reservations/ReservationsPerUser";
import * as json2csv from 'json2csv';
import * as fs from 'fs';

export class CsvGenerator {
  private titles: Array<string>;
	private stationData: Array<JsonObject>;
	private userData: Array<JsonObject>;
  private path: string;
  
  public constructor(path: string) {
    this.titles = new Array();
    this.stationData = new Array();
    this.userData = new Array();
    this.path = path;
  }
    
	 public async generate(data: Map<string, any>): Promise<void> {
      try {
          await this.init(data);
          this.transformToCsv();
      }
      catch(error) {
          throw new Error('Error initializing csv generator: '+error);
      }
      return;
	 }

	public async init(data: Map<string, any>): Promise<void> {
		this.titles.push('id');
//        data.get(ReservationsPerStation.name).forEach( (value, key) => 
//            this.titles.push(value.name));
//        data.get(RentalsAndReturnsPerStation.name).forEach( (value, key) => 
//            this.titles.push(value.name));
        
    let history: HistoryReader = await HistoryReader.create(this.path);
    let stationEntities: HistoryEntitiesJson = await history.getEntities('stations');    
    let stations: Array<Station> = <Station[]> stationEntities.instances;
    let userEntities: HistoryEntitiesJson = await history.getEntities('users');
    let users: Array<User> = <User[]> userEntities.instances;
    
    let reservations, rentalsAndReturns: any;
    let value: number;
    
    reservations = data.get(ReservationsPerStation.name);
    rentalsAndReturns = data.get(RentalsAndReturnsPerStation.name);
    for (let station of stations) {
      let stationJson: JsonObject = {
          "id": station.id  
      };
      value = reservations.getFailedBikeReservations().get(station.id);
      stationJson.bike_failed_reservations = value;
      
      value = reservations.getSlotFailedReservationsOfStation(station.id);
      stationJson.slot_failed_reservations = value;
      
      value = reservations.getBikeSuccessfulReservationsOfStation(station.id);
      stationJson.bike_successful_reservations = value;
      
      value = reservations.getSlotSuccessfulReservationsOfStation(station.id);
      stationJson.slot_successful_reservations = value;
      
      value = rentalsAndReturns.getBikeFailedRentalsOfStation(station.id);
      stationJson.bike_failed_rentals = value;
      
      value = rentalsAndReturns.getBikeFailedReturnsOfStation(station.id);
      stationJson.bike_failed_returns = value;
      
      value = rentalsAndReturns.getBikeSuccessfulRentalsOfStation(station.id);
      stationJson.bike_successful_rentals = value;
      
      value = rentalsAndReturns.getBikeSuccessfulReturnsOfStation(station.id);
      stationJson.bike_successful_returns = value;
      
      this.stationData.push(stationJson);
      
    }
    
    reservations = data.get(ReservationsPerUser.name);
    reservations.print();
    rentalsAndReturns = data.get(RentalsAndReturnsPerUser.name);
    for (let user of users) {
      let userJson: JsonObject = {};
      
      userJson.id = user.id;
      value = reservations.getBikeFailedReservationsOfUser(user.id);
      userJson.bike_failed_reservations = value;
      value = reservations.getSlotFailedReservationsOfUser(user.id);
      userJson.slot_failed_reservations = value;
      value = reservations.getBikeSuccessfulReservationsOfUser(user.id);
      userJson.bike_successful_reservations = value;
      value = reservations.getSlotSuccessfulReservationsOfUser(user.id);
      userJson.slot_successful_reservations = value;
      
      value = rentalsAndReturns.getBikeFailedRentalsOfUser(user.id);
      userJson.bike_failed_rentals = value;
      value = rentalsAndReturns.getBikeFailedReturnsOfUser(user.id);
      userJson.bike_failed_returns = value;
      value = rentalsAndReturns.getBikeSuccessfulRentalsOfUser(user.id);
      userJson.bike_successful_rentals = value;
      value = rentalsAndReturns.getBikeSuccessfulReturnsOfUser(user.id);
      userJson.bike_successful_returns = value;
      
      this.userData.push(userJson);
      
    }
    return;
	}

	public transformToCsv(): void {
    let csv = json2csv({ data: this.stationData, fields: this.titles, withBOM: true });
    fs.writeFile ('stations.csv', csv, (err) => {
      if (err) throw err;
      console.log('stations file saved');
    });
    csv = json2csv({ data: this.userData, fields: this.titles, withBOM: true });
    fs.writeFile ('users.csv', csv, (err) => {
      if (err) throw err;
      console.log('user file saved');
    });    
	}

}  