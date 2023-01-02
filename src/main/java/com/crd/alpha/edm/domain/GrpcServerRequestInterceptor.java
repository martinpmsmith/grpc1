package com.crd.alpha.edm.domain;

import io.grpc.*;


public class GrpcServerRequestInterceptor implements ServerInterceptor {
    public static final Context.Key<String> CX= Context.key("x");
    public static final Context.Key<String> CY= Context.key("y");
    public static final Context.Key<String> CZ= Context.key("z");
    public static final Context.Key<String> CA= Context.key("a");

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> serverCall, Metadata metadata, ServerCallHandler<ReqT, RespT> next) {

//        var userToken = metadata.get(Metadata.Key.of("JWT", Metadata.ASCII_STRING_MARSHALLER));
        Metadata.Key<String> xkey = Metadata.Key.of("x", Metadata.ASCII_STRING_MARSHALLER);
        Metadata.Key<String> ykey = Metadata.Key.of("y", Metadata.ASCII_STRING_MARSHALLER);
        Metadata.Key<String> zkey = Metadata.Key.of("z", Metadata.ASCII_STRING_MARSHALLER);
        Metadata.Key<String> akey = Metadata.Key.of("a", Metadata.ASCII_STRING_MARSHALLER);
        var x = metadata.get(xkey);
        var y = metadata.get(ykey);
        var z = metadata.get(zkey);
        var a = metadata.get(akey);
        Context context = Context.current().withValues(CX, x, CY, y, CZ, z, CA, a );
        return Contexts.interceptCall(context, serverCall, metadata, next);
    }

    private void validateUserToken(String userToken) {
        // Logic to validate token
    }
}