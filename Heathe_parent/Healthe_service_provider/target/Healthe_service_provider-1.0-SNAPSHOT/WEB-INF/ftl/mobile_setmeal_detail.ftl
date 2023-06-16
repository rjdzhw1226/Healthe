<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <!-- ����3��meta��ǩ*����*������ǰ�棬�κ��������ݶ�*����*������� -->
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0,user-scalable=no,minimal-ui">
    <meta name="description" content="">
    <meta name="author" content="">
    <link rel="icon" href="../img/asset-favico.ico">
    <title>ԤԼ����</title>
    <link rel="stylesheet" href="../css/page-health-orderDetail.css" />
    <script src="../plugins/vue/vue.js"></script>
    <script src="../plugins/vue/axios-0.18.0.js"></script>
    <script src="../plugins/healthmobile.js"></script>
</head>
<body data-spy="scroll" data-target="#myNavbar" data-offset="150">
<div id="app" class="app">
    <!-- ҳ��ͷ�� -->
    <div class="top-header">
        <span class="f-left"><i class="icon-back" onclick="history.go(-1)"></i></span>
        <span class="center">���ǽ���</span>
        <span class="f-right"><i class="icon-more"></i></span>
    </div>
    <!-- ҳ������ -->
    <div class="contentBox">
        <div class="card">
            <div class="project-img">
                <img src="http://puco9aur6.bkt.clouddn.com/${setmeal.img}"
                     width="100%" height="100%" />
            </div>
            <div class="project-text">
                <h4 class="tit">${setmeal.name}</h4>
                <p class="subtit">${setmeal.remark}</p>
                <p class="keywords">
                    <span>
						<#if setmeal.sex == '0'>
                            �Ա���
                        <#else>
                            <#if setmeal.sex == '1'>
                                ��
                            <#else>
                                Ů
                            </#if>
                        </#if>
					</span>
                    <span>${setmeal.age}</span>
                </p>
            </div>
        </div>
        <div class="table-listbox">
            <div class="box-title">
                <i class="icon-zhen"><span class="path1"></span><span class="path2"></span></i>
                <span>�ײ�����</span>
            </div>
            <div class="box-table">
                <div class="table-title">
                    <div class="tit-item flex2">��Ŀ����</div>
                    <div class="tit-item  flex3">��Ŀ����</div>
                    <div class="tit-item  flex3">��Ŀ���</div>
                </div>
                <div class="table-content">
                    <ul class="table-list">
                        <#list setmeal.checkGroups as checkgroup>
                            <li class="table-item">
                                <div class="item flex2">${checkgroup.name}</div>
                                <div class="item flex3">
                                    <#list checkgroup.checkItems as checkitem>
                                        <label>
                                            ${checkitem.name}
                                        </label>
                                    </#list>
                                </div>
                                <div class="item flex3">${checkgroup.remark}</div>
                            </li>
                        </#list>
                    </ul>
                </div>
                <div class="box-button">
                    <a @click="toOrderInfo()" class="order-btn">����ԤԼ</a>
                </div>
            </div>
        </div>
    </div>
</div>
<script>
    var vue = new Vue({
        el:'#app',
        methods:{
            toOrderInfo(){
                window.location.href = "orderInfo.html?id=${setmeal.id}";
            }
        }
    });
</script>
</body>