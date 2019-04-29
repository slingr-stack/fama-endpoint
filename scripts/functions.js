/////////////////////
// Private functions
/////////////////////
var get = function(url) {
    var options = checkHttpOptions(url, {});
    return endpoint._get(options);
};

var post = function(url, options) {
    options = checkHttpOptions(url, options);
    return endpoint._post(options);
};

var put = function(url, options) {
    options = checkHttpOptions(url, options);
    return endpoint._put(options);
};

/////////////////////
// Public API
/////////////////////
endpoint.persons = {};

//Person

endpoint.persons.getReport = function(uuid) {
    if (!uuid) {
        throw 'Person identifier is required for this operation', 'argumentException';
    }
    return get('/person/'+uuid);
};

endpoint.persons.getReportPdf = function(uuid, fileName) {
    if (!uuid) {
        throw 'Person identifier is required for this operation', 'argumentException';
    }
    var request = {
        path: '/person/'+uuid+'/report',
        params: {
            username: endpoint._configuration.userName
        },
        forceDownload: true,
        downloadSync: true,
        fileName: fileName || 'report.pdf'
    };
    return get(request);
};

endpoint.persons.create = function(personData) {
    if (!personData) {
        throw 'Person body is required for this operation', 'argumentException';
    }
    return post('/person', personData);
};

endpoint.persons.addSocialMediaProfile = function(uuid, profileData) {
    if (!uuid) {
        throw 'Person identifier is required for this operation', 'argumentException';
    }
    if (!profileData) {
        throw 'Profile body is required for this operation', 'argumentException';
    }
    return post('/profile?uuid='+uuid, profileData);
};


/////////////////////////////////////
//  Private helpers
////////////////////////////////////

var checkHttpOptions = function (url, options) {
    options = options || {};
    if (!!url) {
        if (isObject(url)) {
            // take the 'url' parameter as the options
            options = url || {};
        } else {
            if (!!options.path || !!options.params || !!options.body) {
                // options contains the http package format
                options.path = url;
            } else {
                // create html package
                options = {
                    path: url,
                    body: options
                }
            }
        }
    }
    return options;
};

var isObject = function (obj) {
    return !!obj && stringType(obj) === '[object Object]'
};

var stringType = Function.prototype.call.bind(Object.prototype.toString);