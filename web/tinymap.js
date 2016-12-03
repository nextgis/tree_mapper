


function getXmlHttpObject() {
    if (window.XMLHttpRequest) {
        return new XMLHttpRequest();
    }
    if (window.ActiveXObject) {
        return new ActiveXObject("Microsoft.XMLHTTP");
    }
    return null;
}


var map;
var ajaxRequest;
var plotlist;
var ngwLayerGroup
var plotlayers=[];
var nRequest = new Array;

var LayerDescription = new Array;


if (typeof config == "undefined") {
   console.log('Error: Config file for nextgisweb_tinymap not loaded.');
}
if (typeof pointToLayer == "undefined") {
   console.log('Error: Leaflet map style file for nextgisweb_tinymap not loaded.');
}



var NGWLayerURL = config.NGWLayerURL;

if (config.NGWPhotoThumbnailSize) {

    var NGWPhotoThumbnailSize=config.NGWPhotoThumbnailSize;
}
else
{
    var NGWPhotoThumbnailSize='400x300';
}





function initmap() {

    // set up AJAX request
    ajaxRequest = getXmlHttpObject();
    if (ajaxRequest == null) {
        alert("This browser does not support HTTP Request");
        return;
    }

    nRequest['geodata'] = getXmlHttpObject();
    if (nRequest['geodata'] == null) {
        alert("This browser does not support HTTP Request");
        return;
    }

    nRequest['aliaces'] = getXmlHttpObject();
    if (nRequest['aliaces'] == null) {
        alert("This browser does not support HTTP Request");
        return;
    }

	// set up the map
	map = new L.Map('map');




	// start the map in South-East England
	map.setView(new L.LatLng(120, 37.7),6);


    //Add map layers

    //-----------------------------------------------------------------------------------------------------------  Basemaps

	// create basemap tile layers with attribution --- OSM
	var osmUrl='http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png';
	var osmAttrib='Картографические данные © <a href="http://openstreetmap.org">OpenStreetMap</a>';
	var osm = new L.TileLayer(osmUrl, {minZoom: 0, maxZoom: 18, attribution: osmAttrib});


	// create basemap tile layers with attribution --- Sputnik
	var tmsUrl='http://{s}.tiles.maps.sputnik.ru/{z}/{x}/{y}.png';
	var tmsAttrib='<a href="http://maps.sputnik.ru">Спутник</a> © Ростелеком © <a href="http://wiki.openstreetmap.org">Openstreetmap</a>';
	var tms = new L.TileLayer(tmsUrl, {minZoom: 0, maxZoom: 18, attribution: tmsAttrib});

    // create basemap tile layers with attribution --- Mapbox Gray
	var mbUrl='https://api.mapbox.com/styles/v1/nasnimal/cir3nj27y004kcmkgfnw6u68o/tiles/256/{z}/{x}/{y}/?access_token=pk.eyJ1IjoibmFzbmltYWwiLCJhIjoiY2lvNXcxb29nMDA0YXc2bHkwc2hpNTB2MSJ9.C6eEm-ifqAKsgBIC_5mGZw';
	var mbAttrib='<a href="https://www.mapbox.com/">Mapbox ©</a>';
	var mb = new L.TileLayer(mbUrl, {minZoom: 0, maxZoom: 19, attribution: mbAttrib});

    basemaps={'Openstreetmap':osm,'Sputnik':tms,'Mapbox':mb};
    overlays=config.overlays;

    //Add basemap and tms overlays to layers control
    //L.control.layers(basemaps,overlays).addTo(map);
    basemaps.Mapbox.addTo(map);

    //Add every tms overlays to map
    for (var key in overlays) {
        if (!overlays.hasOwnProperty(key)) continue;
        overlays[key].addTo(map);
    }

    map.fitWorld();

    //Add NGW vector overlay at map
    ngwLayerGroup = L.featureGroup().addTo(map);

	askForPlots();
	map.on('moveend', onMapMove);


    //set map extent to bbox of ngwLayers
    switch (config.DefaultBBOXMode) {
        case 'manual':
        map.setView(new L.LatLng(config.lat, config.lon),config.zoom);

        break;
    default:
        setTimeout(function(){ map.fitBounds(ngwLayerGroup.getBounds().pad(0.8));}, 1500);    //taken from https://groups.google.com/forum/#!topic/leaflet-vector-layers/5Fbhv26mmUI

    }


    //get layer aliases from ngw

    getNGWDescribeFeatureType(NGWLayerURL);
    map.attributionControl.setPrefix(config.NGWLayerAttribution);




}

function addDataToMap(data, map) {
    var dataLayer = L.geoJson(data);

    dataLayer.addTo(map);
}

function askForPlots() {
	// request the marker info with AJAX for the current bounds
	var bounds=map.getBounds();
	var minll=bounds.getSouthWest();
	var maxll=bounds.getNorthEast();

    var msg=NGWLayerURL+'/feature/';

	nRequest['geodata'].onreadystatechange = function() {

	    if (nRequest['geodata'].readyState==4) {
		    if (nRequest['geodata'].status==200) {


                feature = eval("(" + nRequest['geodata'].responseText + ")");
                geojson = feature2geojson(feature);

                ngwLayerGroup.clearLayers();

                proj4.defs("EPSG:3857","+proj=merc +a=6378137 +b=6378137 +lat_ts=0.0 +lon_0=0.0 +x_0=0.0 +y_0=0 +k=1.0 +units=m +nadgrids=@null +wktext  +no_defs");

                geojsonLayer = L.Proj.geoJson(geojson,{
                onEachFeature: onEachFeature,
                pointToLayer: pointToLayer,
                attribution: '',
                });
                ngwLayerGroup.addLayer(geojsonLayer);

		    }
	    }
    }






	nRequest['geodata'].open('GET', msg, true);
	nRequest['geodata'].send(null);
}




function onEachFeature(feature, layer) {
    // does this feature have a property named popupContent?

    if (feature.type && feature.id) {

    layer.on({
        click: whenClicked
    });

    }
}



function GeoJSONGeom2NGWFeatureGeom(geom)
{


parcedWKT=omnivore.wkt.parse(geom);
t=parcedWKT._layers[Object.keys(parcedWKT._layers)[0]].feature.geometry;



return t;
}

function feature2geojson(features){

geojson={};

geojson=JSON.parse('{"crs": {"type": "name", "properties": {"name": "EPSG:3857"}}, "type": "FeatureCollection"}');
geojson['features']=[];



  for (var key in features) {
        feature=features[key];
        fields=feature.fields;
        geojsonFeature={};


        geojsonFeature.type="Feature";
        geojsonFeature['geometry']=GeoJSONGeom2NGWFeatureGeom(feature.geom);
        geojsonFeature['properties']=feature.fields;
        geojsonFeature['id']=feature.id;
        geojsonFeature['extensions']={};
        geojsonFeature['extensions']=feature['extensions'];

        geojson['features'].push(geojsonFeature);

    }

return geojson;
}




//Generate HTML for popup


function getPopupHTML(feature,FieldsDescriptions) {

    data=feature.properties;

    var hideEmptyFields=true;



    //get name for identify

    for (var key in FieldsDescriptions) {
        value=FieldsDescriptions[key];
        if (value.label_field) {
        var featureNameField=key;
        }
    }



    //photo

    var photos=[];

    for (var key in feature.extensions.attachment) {

        attachment=feature.extensions.attachment[key];

        if (attachment.is_image) {
        var featureNameField=key;
        photos.push(attachment);
        }
    }

    // html

    var header = '';

    if (featureNameField) {
        if (data[featureNameField]) {
        var header = header + '<div id="identifyFeatureName">'+data[featureNameField]+'</div>';
        }
    }


    //attributes
    var header = header + '<div  style="height:300px;  overflow-y: auto;">';
    var footer='</div>';
    var content='';

    content=content+'<table>';
    for (var key in data) {
        value = data[key];
        if (FieldsDescriptions[key].grid_visibility) {
            content=content+'<tr><td>'+FieldsDescriptions[key].display_name+'</td><td>'+value+'</td><tr>';
        }
    }
    content=content+'</table>';

    //create hrefs in attributes
    //settings demo: http://gregjacobs.github.io/Autolinker.js/examples/live-example/
    //library source: https://github.com/gregjacobs/Autolinker.js MIT
    var autolinker = new Autolinker( {
    urls : {
        schemeMatches : true,
        wwwMatches    : true,
        tldMatches    : true
    },
    email       : false,
    phone       : false,
    twitter     : false,
    hashtag     : false,

    stripPrefix : true,
    newWindow   : true,

    truncate : {
        length   : 0,
        location : 'end'
    },

    className : ''
} );

    var myLinkedHtml = autolinker.link( content );
    content=myLinkedHtml;



        for (var key in photos) {
        photo=photos[key];
        content=content+'<a target="_blank" href="'+NGWLayerURL+'/feature/'+feature.id+'/attachment/'+photo.id+'/download"><img src="'+NGWLayerURL+'/feature/'+feature.id+'/attachment/'+photo.id+'/image?size='+NGWPhotoThumbnailSize+'" >'+'</img></a>';

    }


    return header+content+footer;

}



function whenClicked(e) {

    //var url=NGWLayerURL+'/feature/'+String(e.target.feature.id);

    //featureData=queryGetFeatureInfo(e);



    var feature;
    var aliases;

    popupHTML = getPopupHTML(e.target.feature,LayerDescription);


    var divNode = document.createElement('DIV');
    divNode.innerHTML = popupHTML;

    var popup = new L.Popup({maxWidth:500});
    popup.setLatLng(e.latlng);
    popup.setContent(divNode);



function onPopupImageLoad() {
    marker._popup._update();
}

/*
var images = popup.contentNode.getElementsByTagName('img');

for (var i = 0, len = images.length; i < len; i++) {
    images[i].onload = onPopupImageLoad;
}

*/
    map.openPopup(popup);







}


//Get fields information

function getNGWDescribeFeatureType(url)
{

    url1=url+'';//sample: http://176.9.38.120/practice2/api/resource/29



	nRequest['aliaces'].onreadystatechange = function() {
if (nRequest['aliaces'].readyState==4) {
		if (nRequest['aliaces'].status==200) {
            data = eval("(" + nRequest['aliaces'].responseText + ")");
            var attrInfo={};

            fieldsInfo=data.feature_layer.fields;

                for (var key in fieldsInfo) {
               attrInfo[fieldsInfo[key].keyname]=fieldsInfo[key];

                }
            LayerDescription = attrInfo;    //put to global variable
            return attrInfo;

		}
	}

}; //end onreadystatechange
	nRequest['aliaces'].open('GET', url1, true);
	nRequest['aliaces'].send(null);
}







function onMapMove(e) { askForPlots(); }

initmap();
