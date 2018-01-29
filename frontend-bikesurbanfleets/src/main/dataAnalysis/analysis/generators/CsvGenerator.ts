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

	public generate(data: Map<string, any>): void {
  this.init(data);
		this.transformToCsv();
	}

	public async init(data: Map<string, any>): Promise<void> {
		this.titles.push('id');
		this.titles.push('bike_failed_reservations');
		this.titles.push('slot_failed_reservations');
		this.titles.push('bike_successful_reservations');
		this.titles.push('slot_successful_reservations');
		this.titles.push('bike_failed_rentals');
		this.titles.push('bike_failed_rentals');
		this.titles.push('bike_successful_rentals');
		this.titles.push('bike_successful_returns');
    
    let history: HistoryReader = await HistoryReader.create(this.path);
    let entities: HistoryEntitiesJson = await history.getEntities('stations');    
    let stations: Array<Station> = <Station[]> entities.instances;
    entities = await history.getEntities('users');
    let users: Array<User> = <User[]> entities.instances;
    
    let stationJson, userJson: JsonObject;
    let reservations, rentalsAndReturns: any;
    let value: number;
    for (let station of stations) {
      reservations = data.get(ReservationsPerStation.name);
      value = reservations.getBikeFailedReservationsOfStation(station.id);
      stationJson.bike_failed_reservations = value;
      value = reservations.getSlotFailedReservationsOfStation(station.id);
      stationJson.slot_failed_reservations = value;
      value = reservations.getBikeSuccessfulReservationsOfStation(station.id);
      stationJson.bike_successful_reservations = value;
      value = reservations.getSlotSuccessfulReservationsOfStation(station.id);
      stationJson.slot_successful_reservations = value;
      
      rentalsAndReturns = data.get(RentalsAndReturnsPerStation.name);
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
    
    for (let user of users) {
      reservations = data.get(ReservationsPerUser.name);
      value = reservations.getBikeFailedReservationsOfUser(user.id);
      userJson.bike_failed_reservation = value;
      value = reservations.getSlotFailedReservationsOfUser(user.id);
      userJson.slot_failed_reservation = value;
      value = reservations.getBikeSuccessfulReservationsOfUser(user.id);
      userJson.bike_successful_reservation = value;
      value = reservations.getSlotSuccessfuulReservationsOfUser(user.id);
      userJson.slot_successful_reservation = value;
      
      rentalsAndReturns = data.get(RentalsAndReturnsPerUser.name);
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

	}

	public transformToCsv(): void {
    let csv = json2csv({ data: this.stationData, fields: this.titles });
    fs.writeFile ('stations.csv', csv, (err) => {
      if (err) throw err;
      console.log('stations file saved');
    });
    csv = json2csv({ data: this.userData, fields: this.titles });
    fs.writeFile ('users.csv', csv, (err) => {
      if (err) throw err;
      console.log('stations file saved');
    });    
	}

}  