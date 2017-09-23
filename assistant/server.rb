require 'java'
require 'json'
require 'sinatra'

java_import 'net.ajmichael.mta.LTrainLookup'

set :port, 7000

directions = {
  "Manhattan" => "N",
  "Brooklyn" => "S",
}

stations = {
  "8th Avenue" => "01",
  "6th Avenue" => "02",
  "Union Square" => "03",
  "3rd Avenue" => "04",
  "1st Avenue" => "05",
  "Bedford Avenue" => "06",
  "Lorimer Street" => "07",
  "Graham Avenue" => "08",
  "Grand Street" => "09",
  "Montrose Avenue" => "10",
  "Morgan Avenue" => "11",
  "Jefferson Street" => "12",
  "DeKalb Avenue" => "13",
  "Myrtle-Wyckoff Avenue" => "14",
  "Halsey Street" => "15",
  "Wilson Avenue" => "16",
  "Bushwick Avenue" => "17",
  "Broadway Junction" => "18",
  "Atlantic Avenue" => "19",
  "Sutter Avenue" => "20",
  "Livonia Avenue" => "21",
  "New Lots Avenue" => "22",
  "East 105 Street" => "23",
  "Canarsie-Rockaway Parkway" => "24",
}

post '/' do
  request.body.rewind
  request_payload = JSON.parse request.body.read
  puts request_payload
  parameters = request_payload["result"]["parameters"]
  direction = parameters["L-train-direction"]
  station = parameters["L-train-station"]
  lookup_code = "L#{stations[station]}#{directions[direction]}"
  puts lookup_code
  lookup = LTrainLookup.new lookup_code
  nextTrains = lookup.nextTrains
  puts nextTrains
  if nextTrains.empty?
    response = "There are no #{direction} bound L trains approaching #{station}"
  else
    minutes = nextTrains.first
    response = "The next #{direction} bound L train will depart #{station} in "\
               "#{minutes} minutes."
    if minutes <= 0 and nextTrains.size > 1
      minutes = nextTrains[1]
      response = "#{response} The following train will depart in #{minutes} "\
                 "minutes."
    end
  end

  content_type :json
  { :speech => response }.to_json
end
