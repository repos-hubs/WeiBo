// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: ResponseProtoc.proto

package com.kindroid.hub.proto;

public final class ResponseProtoc {
  private ResponseProtoc() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
  }
  public static final class Response extends
      com.google.protobuf.GeneratedMessage {
    // Use Response.newBuilder() to construct.
    private Response() {
      initFields();
    }
    private Response(boolean noInit) {}
    
    private static final Response defaultInstance;
    public static Response getDefaultInstance() {
      return defaultInstance;
    }
    
    public Response getDefaultInstanceForType() {
      return defaultInstance;
    }
    
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return com.kindroid.hub.proto.ResponseProtoc.internal_static_com_kindroid_hub_proto_Response_descriptor;
    }
    
    protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.kindroid.hub.proto.ResponseProtoc.internal_static_com_kindroid_hub_proto_Response_fieldAccessorTable;
    }
    
    public enum ResultType
        implements com.google.protobuf.ProtocolMessageEnum {
      SUCCESS(0, 1),
      FAIL(1, 2),
      INVALIDREQUEST(2, 3),
      INVALIDTOKEN(3, 4),
      RESOURCENOTEXIST(4, 5),
      DATAISNULL(5, 6),
      ;
      
      
      public final int getNumber() { return value; }
      
      public static ResultType valueOf(int value) {
        switch (value) {
          case 1: return SUCCESS;
          case 2: return FAIL;
          case 3: return INVALIDREQUEST;
          case 4: return INVALIDTOKEN;
          case 5: return RESOURCENOTEXIST;
          case 6: return DATAISNULL;
          default: return null;
        }
      }
      
      public static com.google.protobuf.Internal.EnumLiteMap<ResultType>
          internalGetValueMap() {
        return internalValueMap;
      }
      private static com.google.protobuf.Internal.EnumLiteMap<ResultType>
          internalValueMap =
            new com.google.protobuf.Internal.EnumLiteMap<ResultType>() {
              public ResultType findValueByNumber(int number) {
                return ResultType.valueOf(number)
      ;        }
            };
      
      public final com.google.protobuf.Descriptors.EnumValueDescriptor
          getValueDescriptor() {
        return getDescriptor().getValues().get(index);
      }
      public final com.google.protobuf.Descriptors.EnumDescriptor
          getDescriptorForType() {
        return getDescriptor();
      }
      public static final com.google.protobuf.Descriptors.EnumDescriptor
          getDescriptor() {
        return com.kindroid.hub.proto.ResponseProtoc.Response.getDescriptor().getEnumTypes().get(0);
      }
      
      private static final ResultType[] VALUES = {
        SUCCESS, FAIL, INVALIDREQUEST, INVALIDTOKEN, RESOURCENOTEXIST, DATAISNULL, 
      };
      public static ResultType valueOf(
          com.google.protobuf.Descriptors.EnumValueDescriptor desc) {
        if (desc.getType() != getDescriptor()) {
          throw new java.lang.IllegalArgumentException(
            "EnumValueDescriptor is not for this type.");
        }
        return VALUES[desc.getIndex()];
      }
      private final int index;
      private final int value;
      private ResultType(int index, int value) {
        this.index = index;
        this.value = value;
      }
      
      static {
        com.kindroid.hub.proto.ResponseProtoc.getDescriptor();
      }
      
      // @@protoc_insertion_point(enum_scope:com.kindroid.hub.proto.Response.ResultType)
    }
    
    // required .com.kindroid.hub.proto.Response.ResultType result = 1;
    public static final int RESULT_FIELD_NUMBER = 1;
    private boolean hasResult;
    private com.kindroid.hub.proto.ResponseProtoc.Response.ResultType result_;
    public boolean hasResult() { return hasResult; }
    public com.kindroid.hub.proto.ResponseProtoc.Response.ResultType getResult() { return result_; }
    
    private void initFields() {
      result_ = com.kindroid.hub.proto.ResponseProtoc.Response.ResultType.SUCCESS;
    }
    public final boolean isInitialized() {
      if (!hasResult) return false;
      return true;
    }
    
    public void writeTo(com.google.protobuf.CodedOutputStream output)
                        throws java.io.IOException {
      getSerializedSize();
      if (hasResult()) {
        output.writeEnum(1, getResult().getNumber());
      }
      getUnknownFields().writeTo(output);
    }
    
    private int memoizedSerializedSize = -1;
    public int getSerializedSize() {
      int size = memoizedSerializedSize;
      if (size != -1) return size;
    
      size = 0;
      if (hasResult()) {
        size += com.google.protobuf.CodedOutputStream
          .computeEnumSize(1, getResult().getNumber());
      }
      size += getUnknownFields().getSerializedSize();
      memoizedSerializedSize = size;
      return size;
    }
    
    public static com.kindroid.hub.proto.ResponseProtoc.Response parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data).buildParsed();
    }
    public static com.kindroid.hub.proto.ResponseProtoc.Response parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data, extensionRegistry)
               .buildParsed();
    }
    public static com.kindroid.hub.proto.ResponseProtoc.Response parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data).buildParsed();
    }
    public static com.kindroid.hub.proto.ResponseProtoc.Response parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data, extensionRegistry)
               .buildParsed();
    }
    public static com.kindroid.hub.proto.ResponseProtoc.Response parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input).buildParsed();
    }
    public static com.kindroid.hub.proto.ResponseProtoc.Response parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input, extensionRegistry)
               .buildParsed();
    }
    public static com.kindroid.hub.proto.ResponseProtoc.Response parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      Builder builder = newBuilder();
      if (builder.mergeDelimitedFrom(input)) {
        return builder.buildParsed();
      } else {
        return null;
      }
    }
    public static com.kindroid.hub.proto.ResponseProtoc.Response parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      Builder builder = newBuilder();
      if (builder.mergeDelimitedFrom(input, extensionRegistry)) {
        return builder.buildParsed();
      } else {
        return null;
      }
    }
    public static com.kindroid.hub.proto.ResponseProtoc.Response parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input).buildParsed();
    }
    public static com.kindroid.hub.proto.ResponseProtoc.Response parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input, extensionRegistry)
               .buildParsed();
    }
    
    public static Builder newBuilder() { return Builder.create(); }
    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder(com.kindroid.hub.proto.ResponseProtoc.Response prototype) {
      return newBuilder().mergeFrom(prototype);
    }
    public Builder toBuilder() { return newBuilder(this); }
    
    public static final class Builder extends
        com.google.protobuf.GeneratedMessage.Builder<Builder> {
      private com.kindroid.hub.proto.ResponseProtoc.Response result;
      
      // Construct using com.kindroid.hub.proto.ResponseProtoc.Response.newBuilder()
      private Builder() {}
      
      private static Builder create() {
        Builder builder = new Builder();
        builder.result = new com.kindroid.hub.proto.ResponseProtoc.Response();
        return builder;
      }
      
      protected com.kindroid.hub.proto.ResponseProtoc.Response internalGetResult() {
        return result;
      }
      
      public Builder clear() {
        if (result == null) {
          throw new IllegalStateException(
            "Cannot call clear() after build().");
        }
        result = new com.kindroid.hub.proto.ResponseProtoc.Response();
        return this;
      }
      
      public Builder clone() {
        return create().mergeFrom(result);
      }
      
      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return com.kindroid.hub.proto.ResponseProtoc.Response.getDescriptor();
      }
      
      public com.kindroid.hub.proto.ResponseProtoc.Response getDefaultInstanceForType() {
        return com.kindroid.hub.proto.ResponseProtoc.Response.getDefaultInstance();
      }
      
      public boolean isInitialized() {
        return result.isInitialized();
      }
      public com.kindroid.hub.proto.ResponseProtoc.Response build() {
        if (result != null && !isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return buildPartial();
      }
      
      private com.kindroid.hub.proto.ResponseProtoc.Response buildParsed()
          throws com.google.protobuf.InvalidProtocolBufferException {
        if (!isInitialized()) {
          throw newUninitializedMessageException(
            result).asInvalidProtocolBufferException();
        }
        return buildPartial();
      }
      
      public com.kindroid.hub.proto.ResponseProtoc.Response buildPartial() {
        if (result == null) {
          throw new IllegalStateException(
            "build() has already been called on this Builder.");
        }
        com.kindroid.hub.proto.ResponseProtoc.Response returnMe = result;
        result = null;
        return returnMe;
      }
      
      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof com.kindroid.hub.proto.ResponseProtoc.Response) {
          return mergeFrom((com.kindroid.hub.proto.ResponseProtoc.Response)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }
      
      public Builder mergeFrom(com.kindroid.hub.proto.ResponseProtoc.Response other) {
        if (other == com.kindroid.hub.proto.ResponseProtoc.Response.getDefaultInstance()) return this;
        if (other.hasResult()) {
          setResult(other.getResult());
        }
        this.mergeUnknownFields(other.getUnknownFields());
        return this;
      }
      
      public Builder mergeFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        com.google.protobuf.UnknownFieldSet.Builder unknownFields =
          com.google.protobuf.UnknownFieldSet.newBuilder(
            this.getUnknownFields());
        while (true) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              this.setUnknownFields(unknownFields.build());
              return this;
            default: {
              if (!parseUnknownField(input, unknownFields,
                                     extensionRegistry, tag)) {
                this.setUnknownFields(unknownFields.build());
                return this;
              }
              break;
            }
            case 8: {
              int rawValue = input.readEnum();
              com.kindroid.hub.proto.ResponseProtoc.Response.ResultType value = com.kindroid.hub.proto.ResponseProtoc.Response.ResultType.valueOf(rawValue);
              if (value == null) {
                unknownFields.mergeVarintField(1, rawValue);
              } else {
                setResult(value);
              }
              break;
            }
          }
        }
      }
      
      
      // required .com.kindroid.hub.proto.Response.ResultType result = 1;
      public boolean hasResult() {
        return result.hasResult();
      }
      public com.kindroid.hub.proto.ResponseProtoc.Response.ResultType getResult() {
        return result.getResult();
      }
      public Builder setResult(com.kindroid.hub.proto.ResponseProtoc.Response.ResultType value) {
        if (value == null) {
          throw new NullPointerException();
        }
        result.hasResult = true;
        result.result_ = value;
        return this;
      }
      public Builder clearResult() {
        result.hasResult = false;
        result.result_ = com.kindroid.hub.proto.ResponseProtoc.Response.ResultType.SUCCESS;
        return this;
      }
      
      // @@protoc_insertion_point(builder_scope:com.kindroid.hub.proto.Response)
    }
    
    static {
      defaultInstance = new Response(true);
      com.kindroid.hub.proto.ResponseProtoc.internalForceInit();
      defaultInstance.initFields();
    }
    
    // @@protoc_insertion_point(class_scope:com.kindroid.hub.proto.Response)
  }
  
  private static com.google.protobuf.Descriptors.Descriptor
    internal_static_com_kindroid_hub_proto_Response_descriptor;
  private static
    com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internal_static_com_kindroid_hub_proto_Response_fieldAccessorTable;
  
  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\024ResponseProtoc.proto\022\026com.kindroid.hub" +
      ".proto\"\270\001\n\010Response\022;\n\006result\030\001 \002(\0162+.co" +
      "m.kindroid.hub.proto.Response.ResultType" +
      "\"o\n\nResultType\022\013\n\007SUCCESS\020\001\022\010\n\004FAIL\020\002\022\022\n" +
      "\016INVALIDREQUEST\020\003\022\020\n\014INVALIDTOKEN\020\004\022\024\n\020R" +
      "ESOURCENOTEXIST\020\005\022\016\n\nDATAISNULL\020\006B\030\n\026com" +
      ".kindroid.hub.proto"
    };
    com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
      new com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner() {
        public com.google.protobuf.ExtensionRegistry assignDescriptors(
            com.google.protobuf.Descriptors.FileDescriptor root) {
          descriptor = root;
          internal_static_com_kindroid_hub_proto_Response_descriptor =
            getDescriptor().getMessageTypes().get(0);
          internal_static_com_kindroid_hub_proto_Response_fieldAccessorTable = new
            com.google.protobuf.GeneratedMessage.FieldAccessorTable(
              internal_static_com_kindroid_hub_proto_Response_descriptor,
              new java.lang.String[] { "Result", },
              com.kindroid.hub.proto.ResponseProtoc.Response.class,
              com.kindroid.hub.proto.ResponseProtoc.Response.Builder.class);
          return null;
        }
      };
    com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
        }, assigner);
  }
  
  public static void internalForceInit() {}
  
  // @@protoc_insertion_point(outer_class_scope)
}
