/*leaflet style*/

var green = L.icon({
    iconUrl: 'icons/pin-green.svg',
    iconSize:     [32, 32], // size of the icon
    iconAnchor:   [16, 16], // point of the icon which will correspond to marker's location
    popupAnchor:  [1, 1] // point from which the popup should open relative to the iconAnchor
});

var yellow = L.icon({
    iconUrl: 'icons/pin-yellow.svg',
    iconSize:     [32, 32], // size of the icon
    iconAnchor:   [16, 16], // point of the icon which will correspond to marker's location
    popupAnchor:  [1, 1] // point from which the popup should open relative to the iconAnchor
});

var orange = L.icon({
    iconUrl: 'icons/pin-orange.svg',
    iconSize:     [32, 32], // size of the icon
    iconAnchor:   [16, 16], // point of the icon which will correspond to marker's location
    popupAnchor:  [1, 1] // point from which the popup should open relative to the iconAnchor
});

var red = L.icon({
    iconUrl: 'icons/pin-red.svg',
    iconSize:     [32, 32], // size of the icon
    iconAnchor:   [16, 16], // point of the icon which will correspond to marker's location
    popupAnchor:  [1, 1] // point from which the popup should open relative to the iconAnchor
});

var gray = L.icon({
    iconUrl: 'icons/pin-gray.svg',
    iconSize:     [32, 32], // size of the icon
    iconAnchor:   [16, 16], // point of the icon which will correspond to marker's location
    popupAnchor:  [1, 1] // point from which the popup should open relative to the iconAnchor
});

var pointToLayer =  function(feature, latlng){
        switch (feature.properties.state) {
            case 'Здоровое':    return L.marker(latlng, {icon: green});
            case 'Ослабленное':    return L.marker(latlng, {icon: yellow});
            case 'Сильно ослабленное':    return L.marker(latlng, {icon: orange});
            case 'Отмирающее':    return L.marker(latlng, {icon: red});
            case 'Сухостой':    return L.marker(latlng, {icon: gray});
            default:        return L.marker(latlng, {icon: green});
        }
}
