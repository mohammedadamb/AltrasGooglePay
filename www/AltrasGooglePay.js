var exec = require("cordova/exec");

module.exports.coolMethod = function (arg0, success, error) {
  exec(success, error, "AltrasGooglePay", "coolMethod", [arg0]);
};

module.exports.initGooglePay = function (arg0, success, error) {
  exec(success, error, "AltrasGooglePay", "initGooglePay", [arg0]);
};

module.exports.canUseGooglePay = function (arg0, success, error) {
  exec(success, error, "AltrasGooglePay", "canUseGooglePay", [arg0]);
};
