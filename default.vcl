vcl 4.0;

backend default {
    .host = "localhost";
    .port = "7800";
}

sub vcl_recv {
    if (req.url ~ "^/tiles/") {
        set req.url = regsub(req.url, "^/tiles/", "/");
    }
}

sub vcl_backend_response {
    if (bereq.url ~ "^/tiles/") {
        set beresp.ttl = 1h;
        set beresp.grace = 1h;
    }
}

sub vcl_deliver {
    set resp.http.X-Cache = "HIT";
}
