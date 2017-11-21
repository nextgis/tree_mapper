

var config = {
    NGWLayerURL: 'http://citywoody.nextgis.com/api/resource/10',
    NGWPhotoThumbnailSize : '400x300',
    NGWLayerAttribution:'<a href="http://nextgis.ru/">NextGIS</a>',

//   DefaultBBOXMode:'manual',        //Optional
//    lat:43.8,                       //Optional
//    lon:134.6,                      //Optional
//    zoom:8,                         //Optional

}

    config.overlays={

        //Optional TMS overlays
        //'Границы'  :new L.TileLayer('http://opendata25.primorsky.ru/ngw/api/component/render/tile?resource=535&z={z}&x={x}&y={y}', {minZoom: 0, maxZoom: 18, attribution: 'Слой АТД: Администрация Приморского края'}),

    };
