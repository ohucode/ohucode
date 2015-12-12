(function (scope, $) {
    var App = Vue.extend();

    var ValidationRegex = {
        email: /^(([^<>()[\]\\.,;:\s@\"]+(\.[^<>()[\]\\.,;:\s@\"]+)*)|(\".+\"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/,
        userid: /\w{4,16}/
    };

    var VR = scope.VR = ValidationRegex;

    var is_valid_model = function(model) {
        return model.pattern.test(model.value);
    };
    
    var validation_class = function(model) {
        var empty = model.value == "";
        var valid = is_valid_model(model);
        return {
            'has-error': !empty && !valid,
            'has-success': !empty && valid
        };
    };

    Vue.filter('validation_class', validation_class);

    new Vue({
        el: "#signup-form",
        data: {
            email: {
                value: "",
                pattern: VR.email
            },
            userid: {
                value: "",
                pattern: VR.userid
            }
        },
        computed: {
            valid_email: function() { return is_valid_model(this.email); },
            valid_userid: function() { return is_valid_model(this.userid); },
            valid_form: function() {
                return this.valid_email && this.valid_userid;
            }
        }
    });
})(window, jQuery);
