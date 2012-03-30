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
					mapTypeId: google.maps.MapTypeId.HYBRID
				});

	  // Use this code to set a new layer as a baselayer -
	  // which means that it'll be on the bottom of any other
	  // layers and you won't see Google tiles
	  map.mapTypes.set('mb', new wax.g.connector(tilejson));
	  map.setMapTypeId('mb');

	  // Or use this code to add it as an overlay
	  // m.overlayMapTypes.insertAt(0, new wax.g.connector(tilejson));
	});
	
	var marker = new google.maps.Marker({
		position: latlng,
		map: map,
		title:"Hello World!"
	});
});