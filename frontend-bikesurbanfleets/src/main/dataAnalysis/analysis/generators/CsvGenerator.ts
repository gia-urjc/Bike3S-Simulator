import * as csv_parser from 'json2csv';

export class CsvGenerator {
	private titles: Array<string>;
	private stationData: Array<JsonObject>;
	private userData: Array<JsonObject>;

	public static create(data: Map<string, any>): void {
		let generator: CsvGenerator = new CsvGenerator();
		generator.init(data);
		generator.transformToCsv();
	}

	public async init(data: Map<string, any>): void {
		this.titles.push('id');
		this.titles.push('bike failed reservations');
		this.titles.push('slot failed reservations');
		this.titles.push('bike successfulreservations');
		this.titles.push('slot successfulreservations');
		this.tittles.push('bike failed rentals');
		this.tittles.push('bike failed rentals');
		this.tittles.push('bike successful rentals');
		this.tittles.push('bike successful returns');

	data.getStations().forEach( (value, key) => { 
		});

	dta.getUsers().forEach (value, key) => {
		});
	}

	public transformToCsv(): void {
	}

}

  