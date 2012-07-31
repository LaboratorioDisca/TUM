$(document).ready(function() {
	var map;
	var latlng = new google.maps.LatLng(19.322675, -99.192080);
	wax.tilejson('http://132.248.51.251:8888/v2/UNAMCU.json',
	  function(tilejson) {
	  map = new google.maps.Map(
	    document.getElementById('map'), {
					zoom: 15,
					center: latlng,
					maxZoom: 20,
					minZoom: 14,
					mapTypeId: google.maps.MapTypeId.HYBRID,
					mapTypeControl: false
				});

	  // Use this code to set a new layer as a baselayer -
	  // which means that it'll be on the bottom of any other
	  // layers and you won't see Google tiles
	  map.mapTypes.set('mb', new wax.g.connector(tilejson));
	  map.setMapTypeId('mb');

	  // Or use this code to add it as an overlay
	  // m.overlayMapTypes.insertAt(0, new wax.g.connector(tilejson));
	});
	
	var vehicleMarkers = {};
	var vehicleInstants = {};
	var lines = {};

	var retrieveVehicles = function() {
		// all vehicles
		$.get("vehicles", function(data){
			for(var idx in data) {
				var line = lines[data[idx].lineId];
				var marker = new google.maps.Marker({
					title: "Vehiculo # "+data[idx].identifier,
					icon: 'http://132.248.51.251:9000/assets/images/'+line.simpleIdentifier+'.png'
				});
				var vehicleId = data[idx].id;
				
				vehicleMarkers[vehicleId] = marker;
				vehicleInstants[vehicleId] = null;

			}
		});
	}
	
	$.get("transports/8/lines", function(data){
		for(var idx in data) {
			var lineId = data[idx].id;
			
			delete data[idx].id;
			lines[lineId] = data[idx];
		}
		retrieveVehicles();
	});
	
	
	function retrieveRecent() {
		$.get("instants/recent", function(data){
			for(var idx in data) {
				var vehicleMarker = vehicleMarkers[data[idx].vehicleId];
				if(vehicleMarker != undefined) {
					updateMarker(vehicleMarker, data[idx]);
				}
			}
		});
	}
	
	setInterval(retrieveRecent, 4000);

	var updateMarker = function(marker, object) {
		
		var latlng = new google.maps.LatLng(object.coordinate.lat, object.coordinate.lon);
		if(marker.getMap() != null) {
			marker.setMap(null);
		}
		marker.setPosition(latlng);
		
		var date = new Date(object.createdAt).toLocaleDateString() +" - "+ new Date(object.createdAt).toLocaleTimeString();
		
		marker.setTitle("Velocidad: " + object.speed + "(km/h) Recibido: " + date);
		marker.setMap(map);
	}
	
});
