/*
 * Copyright 2015 The gRPC Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.crd.alpha.edm.domain;

import com.google.protobuf.Any;
import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Message;
import com.google.rpc.Code;
import com.google.rpc.Status;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptors;
import io.grpc.protobuf.StatusProto;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Server that manages startup/shutdo[wn of a {@code Greeter} server.
 */
public class DomainService {
    private static final Logger logger = Logger.getLogger(DomainService.class.getName());

    private Server server;

    /**
     * Main launches the server from the command line.
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        final DomainService server = new DomainService();
        server.start();
        server.blockUntilShutdown();
    }

    private static void processEntityData(Entity.EntityData entityData) {
        for (Entity.KevValuePair kvp : entityData.getKvpList()) {
            Entity.DataValue.ValueCase vcase = kvp.getValue().getValueCase();

            switch (vcase) {
                case INT_VALUE:
                    break;
                case BOOL_VALUE:
                    break;
                case DOUBLE_VALUE:
                    break;
                case LONG_VALUE:
                    break;
                case BYTES_VALUE:  //ByteString
                    break;
                case FLOAT_VALUE:
                    break;
                case STRING_VALUE:
                    break;
                case VALUE_NOT_SET:
                    break;
            }
        }
        for (Entity.EntityData child : entityData.getChildList()) {
            DomainService.processEntityData(child);
        }
    }

    private void start() throws IOException {
        /* The port on which the server should run */
        int port = 50051;
        server = ServerBuilder.forPort(port)
                .addService(
                        ServerInterceptors.intercept(
                                new DomainServiceImpl(),
                                new GrpcServerRequestInterceptor()))
                .build()
                .start();
        logger.info("Server started, listening on " + port);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                try {
                    DomainService.this.stop();
                } catch (InterruptedException e) {
                    e.printStackTrace(System.err);
                }
                System.err.println("*** server shut down");
            }
        });
    }

    private void stop() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    static class DomainServiceImpl extends DomainServiceGrpc.DomainServiceImplBase {

        private static void extracted(GeneratedMessageV3 req, StreamObserver<?> responseObserver, Exception e) {
            Status status = Status.newBuilder()
                    .setCode(Code.INTERNAL.getNumber())
                    .setMessage("\n" + e.getMessage() + "\n----- request -----\n" + req.toString() + "-------------------\n")
                    .addDetails(Any.pack(req))
                    .build();
            responseObserver.onError(StatusProto.toStatusRuntimeException(status));
        }

        @Override
        public void sendEntityData(Entity.EntityData request, StreamObserver<Entity.EntityData> responseObserver) {
            System.out.println(request);
            DomainService.processEntityData(request);
        }

        @Override
        public void sendTestEntity(Domain.TestEntity request,
                                   StreamObserver<Domain.TestEntity> responseObserver) {
            TestEntity mp = new TestEntity();
            mp.setBoolVal(true);
            mp.setPrimaryKey(12L);
            mp.setDoubleVal(12.2323);
            mp.setFloatVal(12.123f);
            mp.setIntVal(12);
            mp.setThisWasHere("existing value");
            mp.setLongVal(12L);
            mp.setSoWasI(1234L);
            mp.setStringVal("this is a string");

            List<Map<String, Object>> rows = new ArrayList();
            Map<String, Object> row = new HashMap<>();
            row.put("primary_key", 12L);
            row.put("bool_val", true);
            row.put("double_val", 12.2323);
            row.put("float_val", 12.123f);
            row.put("int_val", 12);
            row.put("this_was_here", "existing value");
            row.put("so_was_i", 1234L);
            row.put("long_val", 12L);
            row.put("string_val", "this is a string");
            Map<String, Object> row2 = new HashMap<>();
            row2.put("primary_key", 14L);
            row2.put("bool_val", false);
            row2.put("double_val", null);
            row2.put("float_val", 213.123f);
            row2.put("int_val", 12333);
            row2.put("this_was_here", null);
            row2.put("so_was_i", 1234L);
            row2.put("long_val", 12L);
            row2.put("string_val", "");
            rows.add(row);
            rows.add(row2);

            Message result = null;
            try {
                result = EntityMapper.pojoToProto(mp, Domain.TestEntity.class);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            TestEntity mp2 = (TestEntity) EntityMapper.protoToEntityBase(result, TestEntity.class);

            Map<String, String> queries = EntityMapper.insertQueriesForEntityBase(mp);
            Map<String, String> queries2 = EntityMapper.updateQueriesForEntityBase(mp);
            List<EntityBase> data = EntityMapper.entityBaseListFromQueryResult(TestEntity.class, rows);

            logger.info("\n\n" + result.toString());
            logger.info("\n\n" + mp2.toString());
            logger.info("\n\n" + queries.get("entity_test").toString());
            logger.info("\n\n" + queries2.get("entity_test").toString());

            int rowNo = 1;
            for (EntityBase ent : data) {
                logger.info("\n\n----- row# " + rowNo + "\n" + ent.toString());
                rowNo++;
            }


            int x = 1;
        }
    }
}