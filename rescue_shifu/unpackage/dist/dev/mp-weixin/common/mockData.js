"use strict";
const common_vendor = require("./vendor.js");
class MyOrder extends UTS.UTSType {
  static get$UTSMetadata$() {
    return {
      kind: 2,
      get fields() {
        return {
          id: { type: String, optional: false },
          orderNo: { type: String, optional: false },
          createTime: { type: Number, optional: false },
          serviceType: { type: String, optional: false },
          plateNo: { type: String, optional: false },
          status: { type: "Unknown", optional: false },
          dispatchStatus: { type: "Unknown", optional: false },
          dispatchToName: { type: String, optional: false },
          orderPrice: { type: Number, optional: false },
          ratio: { type: Number, optional: true },
          remark: { type: String, optional: false },
          customerPhone: { type: String, optional: false },
          customerName: { type: String, optional: false },
          address: { type: String, optional: false },
          lat: { type: Number, optional: false },
          lng: { type: Number, optional: false },
          agencyId: { type: String, optional: false },
          agencyOrderType: { type: Number, optional: true },
          masterId: { type: String, optional: false },
          orderReceivingId: { type: String, optional: false },
          images: { type: UTS.UTSType.withGenerics(Array, [String]), optional: false },
          feedbackImages: { type: UTS.UTSType.withGenerics(Array, [String]), optional: false },
          serverStatus: { type: String, optional: true },
          isTransferPending: { type: Boolean, optional: true },
          transferReason: { type: String, optional: true }
        };
      },
      name: "MyOrder"
    };
  }
  constructor(options, metadata = MyOrder.get$UTSMetadata$(), isJSONParse = false) {
    super();
    this.__props__ = UTS.UTSType.initProps(options, metadata, isJSONParse);
    this.id = this.__props__.id;
    this.orderNo = this.__props__.orderNo;
    this.createTime = this.__props__.createTime;
    this.serviceType = this.__props__.serviceType;
    this.plateNo = this.__props__.plateNo;
    this.status = this.__props__.status;
    this.dispatchStatus = this.__props__.dispatchStatus;
    this.dispatchToName = this.__props__.dispatchToName;
    this.orderPrice = this.__props__.orderPrice;
    this.ratio = this.__props__.ratio;
    this.remark = this.__props__.remark;
    this.customerPhone = this.__props__.customerPhone;
    this.customerName = this.__props__.customerName;
    this.address = this.__props__.address;
    this.lat = this.__props__.lat;
    this.lng = this.__props__.lng;
    this.agencyId = this.__props__.agencyId;
    this.agencyOrderType = this.__props__.agencyOrderType;
    this.masterId = this.__props__.masterId;
    this.orderReceivingId = this.__props__.orderReceivingId;
    this.images = this.__props__.images;
    this.feedbackImages = this.__props__.feedbackImages;
    this.serverStatus = this.__props__.serverStatus;
    this.isTransferPending = this.__props__.isTransferPending;
    this.transferReason = this.__props__.transferReason;
    delete this.__props__;
  }
}
const currentUserId = common_vendor.ref(common_vendor.index.getStorageSync("master_user_id") || "");
const globalOrders = common_vendor.reactive([
  new MyOrder({
    id: "1",
    orderNo: "RO202602050001",
    createTime: Date.now(),
    serviceType: "搭电",
    remark: "车辆无法启动，位于地库 B2",
    plateNo: "粤B12345",
    customerPhone: "13800138000",
    customerName: "张先生",
    address: "深圳市南山区科技园北区",
    lat: 22.53332,
    lng: 113.93041,
    orderPrice: 120,
    status: "new",
    dispatchStatus: "none",
    dispatchToName: "",
    agencyId: "SYSTEM_ADMIN",
    masterId: "",
    orderReceivingId: "",
    images: [],
    feedbackImages: []
  }),
  new MyOrder({
    id: "2",
    orderNo: "RO202602050002",
    createTime: Date.now() - 15 * 60 * 1e3,
    serviceType: "换胎",
    remark: "右前轮爆胎，需自带工具",
    plateNo: "粤B54321",
    customerPhone: "13900139000",
    customerName: "李女士",
    address: "深圳市福田区会展中心 3 号门",
    lat: 22.54053,
    lng: 114.05956,
    orderPrice: 150,
    status: "new",
    dispatchStatus: "none",
    dispatchToName: "",
    agencyId: "SYSTEM_ADMIN",
    masterId: "",
    orderReceivingId: "",
    images: [],
    feedbackImages: []
  }),
  new MyOrder({
    id: "3",
    orderNo: "RO202602050003",
    createTime: Date.now() - 30 * 60 * 1e3,
    serviceType: "新能源紧急充电",
    remark: "电量剩余 1%，急需补能",
    plateNo: "粤B88888",
    customerPhone: "13700137000",
    customerName: "陈先生",
    address: "罗湖区深南东路 5002 号",
    lat: 22.547,
    lng: 114.117,
    orderPrice: 200,
    status: "new",
    dispatchStatus: "none",
    dispatchToName: "",
    agencyId: "SYSTEM_ADMIN",
    masterId: "",
    orderReceivingId: "",
    images: [],
    feedbackImages: []
  }),
  new MyOrder({
    id: "11",
    orderNo: "RO202602040011",
    createTime: Date.now() - 3 * 60 * 60 * 1e3,
    serviceType: "道路救援-拖车",
    plateNo: "粤B88888",
    status: "processing",
    dispatchStatus: "dispatched",
    dispatchToName: "张师傅",
    orderPrice: 368,
    remark: "车胎爆了，需要拖车",
    customerPhone: "13800138000",
    customerName: "王先生",
    address: "深圳市南山区腾讯大厦",
    lat: 22.54051,
    lng: 113.93449,
    agencyId: "USER_PROVIDER_123",
    masterId: "USER_MASTER_456",
    orderReceivingId: "",
    images: [],
    feedbackImages: []
  }),
  new MyOrder({
    id: "12",
    orderNo: "RO202602030012",
    createTime: Date.now() - 26 * 60 * 60 * 1e3,
    serviceType: "道路救援-搭电",
    plateNo: "粤B66666",
    status: "done",
    dispatchStatus: "none",
    dispatchToName: "",
    orderPrice: 128,
    remark: "无法启动",
    customerPhone: "13900139000",
    customerName: "李女士",
    address: "深圳市宝安区壹方城",
    lat: 22.55321,
    lng: 113.88707,
    agencyId: "USER_PROVIDER_123",
    masterId: "",
    orderReceivingId: "",
    images: [],
    feedbackImages: []
  }),
  new MyOrder({
    id: "13",
    orderNo: "RO202602050013",
    createTime: Date.now() - 10 * 60 * 1e3,
    serviceType: "道路救援-补胎",
    plateNo: "粤B77777",
    status: "processing",
    dispatchStatus: "none",
    dispatchToName: "",
    orderPrice: 88,
    remark: "扎钉子了",
    customerPhone: "13700137000",
    customerName: "陈先生",
    address: "深圳市福田区平安金融中心",
    lat: 22.53307,
    lng: 114.05454,
    agencyId: "OTHER_PROVIDER",
    masterId: "USER_PROVIDER_123",
    orderReceivingId: "",
    images: [],
    feedbackImages: []
  })
]);
exports.MyOrder = MyOrder;
exports.currentUserId = currentUserId;
exports.globalOrders = globalOrders;
//# sourceMappingURL=../../.sourcemap/mp-weixin/common/mockData.js.map
