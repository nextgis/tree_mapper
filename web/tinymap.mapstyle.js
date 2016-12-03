/*leaflet style*/

var green = L.icon({
    iconUrl: 'icons/pin-green.png',
    iconSize:     [32, 32], // size of the icon
    iconAnchor:   [16, 16], // point of the icon which will correspond to marker's location
    popupAnchor:  [1, 1] // point from which the popup should open relative to the iconAnchor
});

var yellow = L.icon({
    iconUrl: 'icons/pin-yellow.png',
    iconSize:     [32, 32], // size of the icon
    iconAnchor:   [16, 16], // point of the icon which will correspond to marker's location
    popupAnchor:  [1, 1] // point from which the popup should open relative to the iconAnchor
});

var orange = L.icon({
    iconUrl: 'icons/pin-orange.png',
    iconSize:     [32, 32], // size of the icon
    iconAnchor:   [16, 16], // point of the icon which will correspond to marker's location
    popupAnchor:  [1, 1] // point from which the popup should open relative to the iconAnchor
});

var red = L.icon({
    iconUrl: 'icons/pin-red.png',
    iconSize:     [32, 32], // size of the icon
    iconAnchor:   [16, 16], // point of the icon which will correspond to marker's location
    popupAnchor:  [1, 1] // point from which the popup should open relative to the iconAnchor
});

var gray = L.icon({
    iconUrl: 'icons/pin-gray.png',
    iconSize:     [32, 32], // size of the icon
    iconAnchor:   [16, 16], // point of the icon which will correspond to marker's location
    popupAnchor:  [1, 1] // point from which the popup should open relative to the iconAnchor
});

var pointToLayer =  function(feature, latlng){
        switch (feature.properties.state) {
            case 'здоровое':    return L.marker(latlng, {icon: green});
            case 'ослабленное':    return L.marker(latlng, {icon: yellow});
            case 'сильно ослабленное':    return L.marker(latlng, {icon: orange});
            case 'отмирающее':    return L.marker(latlng, {icon: red});
            case 'сухостой':    return L.marker(latlng, {icon: gray});
            default:        return L.marker(latlng, {icon: green});
        }
}
