<?php
include('db_con.php');
header('Content-type:application/json');
include('token.php');

$user_id = $_REQUEST["userid"];
$token_noti= $_REQUEST["token_noti"];
$mysql_qry = "select * from tokens_notification where user_id=?";
// $result=mysqli_query($conn ,$mysql_qry);

$result = mysqli_prepare($conn ,$mysql_qry);
mysqli_stmt_bind_param($result,'s',$user_id);
mysqli_stmt_execute($result);
$result= mysqli_stmt_get_result($result);

$row=mysqli_fetch_array($result);
$ret=array();
if($row){
    $mysql_qry = "update tokens_notification set token_body=? where user_id=?";
    // $row2=mysqli_query($conn ,$mysql_qry2);
    
    $result = mysqli_prepare($conn ,$mysql_qry);
    mysqli_stmt_bind_param($result,'ss',$token_noti,$user_id);
    mysqli_stmt_execute($result);
    // $result= mysqli_stmt_get_result($result);
    
    $ret['status']="updatesuccess";
    
}else{
    
    $mysql_qry = "insert into tokens_notification(token_body,user_id) values (?,?)";
    // $row3=mysqli_query($conn ,$mysql_qry3);
    
    $result = mysqli_prepare($conn ,$mysql_qry);
    mysqli_stmt_bind_param($result,'ss',$token_noti,$user_id);
    mysqli_stmt_execute($result);
    
    $ret['status']="createsuccess";
}
echo json_encode($ret);
?>