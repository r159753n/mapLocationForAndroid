var exec = require('cordova/exec');

exports.getLocation = function (successCallback, errorCallback,args) {
    exec(successCallback, errorCallback, 'LocationPlugin', 'getLocation', [args]);
};