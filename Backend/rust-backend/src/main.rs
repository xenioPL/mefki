#![feature(proc_macro_hygiene, decl_macro)]

#[macro_use]
extern crate rocket;
extern crate csv;

use std::io::Read;

extern crate reqwest;

use rocket::State;
use reqwest::{Response, IntoUrl, Client};
use csv::ReaderBuilder;
use std::collections::HashSet;
use rocket::response::content;


struct MevoData {
    raw_data: String
}

pub fn get<T: IntoUrl>(url: T) -> reqwest::Result<Response> {
    Client::builder()
        .build()?
        .get(url)
        .header("User-Agent", "Mozilla/5.0 (Linux; U; Android 2.2) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1")
        .send()
}

fn download_data() -> String {
    let mut res: Response;
    match get("http://rowermevo.pl/maps/locations.txt?key=jF5puHQe3CqssPZh") {
        Ok(response) => { res = response; }
        Err(err) => {
            println!("{}", err);
            return String::from(""); }
    }
    let mut body = String::new();
    match res.read_to_string(&mut body) {
        Ok(_) => {}
        Err(err) => {
            println!("{}", err);
        }
    }
    println!("{}", body);
    return body;
}

fn format_output(id : String, lat : String, lon : String )-> String{
    format!("{{ id: {}, latitude : {}, longitude: {} }}, ", id, lat, lon)
}

fn format_output2(id : String, number : String)-> String{
    format!("{{ id: {}, number : {} }}, ", id, number)
}


#[get("/getStations")]
fn index(data: State<MevoData>) -> content::Json<String> {
    let mut output: String = "".to_string();
    let mut rdr = ReaderBuilder::new()
        .delimiter(b',')
        .from_reader(data.raw_data.as_bytes());

    for result in rdr.records() {
        match result {
            Ok(data) => {
                let mut id: String = "".to_string();
                let  mut latitude: String = "".to_string();
                let mut longitude: String = "".to_string();

                match data.get(0) {
                    None => {}
                    Some(id_) => { id = id_.to_string(); }
                }

                match data.get(2) {
                    None => {
                    }
                    Some(coridantes) => {
                        let mut splitted = coridantes.split(",");
                        match splitted.next() {
                            Some(raw) => {latitude = raw.to_string()}
                            None => {}
                        }
                        match splitted.next() {
                            Some(raw) => {longitude = raw[1..].to_string()}
                            None => {}
                        }
                    }
                }

                output.push_str(&format_output( id, latitude, longitude));
            }
            Err(_) => {}
        }
    }
    content::Json(format!("[ {} ]", output.trim_end_matches( ", ")))
}

#[get("/getNumberFromStations?<stations>")]
fn numbers_from_stations(data: State<MevoData>, stations: String) -> content::Json<String> {
    let mut stations_set : HashSet<String, _> = HashSet::new();

    for s in stations.split(",") {
        stations_set.insert(s.to_string());
    }

    let mut output: String = "".to_string();
    let mut rdr = ReaderBuilder::new()
        .delimiter(b',')
        .from_reader(data.raw_data.as_bytes());

    for result in rdr.records() {
        match result {
            Ok(data) => {
                let mut id: String = "".to_string();
                let  mut number: String = "".to_string();

                match data.get(0) {
                    None => {}
                    Some(id_) => { id = id_.to_string(); }
                }

                match data.get(1) {
                    None => {}
                    Some(number_) => { number = number_.to_string(); }
                }
                if stations_set.contains(&id.to_string()) {
                    output.push_str(&format_output2(id, number));
                }
            }
            Err(_) => {}
        }
    }
    content::Json(format!("[ {} ]", output.trim_end_matches( ", ")))
}

fn main() {
    let data = download_data();

    rocket::ignite()
        .manage(MevoData { raw_data: data })
        .mount("/", routes![index])
        .mount("/", routes![numbers_from_stations])
        .launch();
}
