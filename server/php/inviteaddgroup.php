<?php

include("db_con.php");
include('token.php');
header('Content-type:application/json');
$group_id = $_REQUEST["groupid"];
$friend_id = $_REQUEST["friendid"];

//$result = mysqli_query($conn,"SELECT users.user_id FROM users LEFT JOIN groups_users ON users.user_id=groups_users.user_id WHERE groups_users.user_id='$friend_id' and groups_users.group_id='$group_id'");
$mysql_qry="SELECT users.user_id FROM users LEFT JOIN groups_users ON users.user_id=groups_users.user_id WHERE groups_users.user_id=? and groups_users.group_id=?";

$result = mysqli_prepare($conn ,$mysql_qry);
mysqli_stmt_bind_param($result,'ss',$friend_id,$group_id);
mysqli_stmt_execute($result);
$result= mysqli_stmt_get_result($result);

$row=mysqli_fetch_array($result);
$ret=array();

if(!$row){
    
    $mysql_qry2 = "insert into groups_users(group_id,user_id,addby_userid) values (?,?,?)";
    // $result1 = mysqli_query($conn ,$mysql_qry2);
    
    $result1 = mysqli_prepare($conn ,$mysql_qry2);
    mysqli_stmt_bind_param($result1,'sss',$group_id,$friend_id,$user_id);
    mysqli_stmt_execute($result1);
    $result1= mysqli_stmt_get_result($result1);
    
    $ret['status']="success";
    $ret['message']="Invite Friend success";
    
}
else{
    
    $ret['status']="fail";
    $ret['message']="Invite friend fail";
    
}

echo json_encode($ret);

?>