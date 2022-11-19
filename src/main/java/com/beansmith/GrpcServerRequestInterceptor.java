package com.beansmith;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;


public class GrpcServerRequestInterceptor implements ServerInterceptor {

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> serverCall, Metadata metadata, ServerCallHandler<ReqT, RespT> next) {

        var userToken = metadata.get(Metadata.Key.of("JWT", Metadata.ASCII_STRING_MARSHALLER));
        validateUserToken(userToken);
        return next.startCall(serverCall, metadata);
    }

    private void validateUserToken(String userToken) {
        // Logic to validate token
    }
}