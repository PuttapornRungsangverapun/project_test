<?php

include("db_con.php");
include('token.php');
header('Content-type:application/json');
$group_name = $_REQUEST["groupname"];
$friend_id = $_REQUEST["frienid"];

//$result = mysqli_query($conn,"SELECT groups.group_name FROM groups WHERE groups.group_name = '".$group_name."'");
$mysql_qry="SELECT groups.group_name FROM groups WHERE groups.group_name = ?";
$result = mysqli_prepare($conn ,$mysql_qry);
mysqli_stmt_bind_param($result,'s',$group_name);
mysqli_stmt_execute($result);
$result= mysqli_stmt_get_result($result);
$row=mysqli_fetch_array($result);
$ret=array();

if(!$row){
    
    $mysql_qry = "insert into groups(group_name) values (?)";
    //$result = mysqli_query($conn ,$mysql_qry);
    $result = mysqli_prepare($conn ,$mysql_qry);
    mysqli_stmt_bind_param($result,'s',$group_name);
    mysqli_stmt_execute($result);
    $result= mysqli_stmt_get_result($result);
    
    $groupid = mysqli_insert_id($conn);
    
    $mysql_qry2 = "insert into groups_users(group_id,user_id) values (?,?)";
    //$result1 = mysqli_query($conn ,$mysql_qry2);
    $result1 = mysqli_prepare($conn ,$mysql_qry2);
    mysqli_stmt_bind_param($result1,'ss',$groupid,$user_id);
    mysqli_stmt_execute($result1);
    $result1= mysqli_stmt_get_result($result1);
    
    $mysql_qry3 = "insert into groups_users(group_id,user_id) values (?,?)";
    //$result2 = mysqli_query($conn ,$mysql_qry3);
    $result2 = mysqli_prepare($conn ,$mysql_qry3);
    mysqli_stmt_bind_param($result2,'ss',$groupid,$friend_id);
    mysqli_stmt_execute($result2);
    $result2= mysqli_stmt_get_result($result2);
    
    $ret['status']="success";
    $ret['groupid']=$groupid ;
    $ret['message']="Create group success";
    
    
    
}
else if($row){
    
    //$result2 = mysqli_query($conn,"SELECT groups.group_name FROM groups WHERE groups.group_name = '".$group_name."'");
    //$rowid=mysqli_fetch_array($result);
    
    //$result = mysqli_query($conn,"SELECT groups.group_id gid FROM groups WHERE groups.group_name = '".$group_name."'");
    $mysql_qry="SELECT groups.group_id gid FROM groups WHERE groups.group_name = ?";
    $result = mysqli_prepare($conn ,$mysql_qry);
    mysqli_stmt_bind_param($result,'s',$group_name);
    mysqli_stmt_execute($result);
    $result= mysqli_stmt_get_result($result);
    $row2=mysqli_fetch_array($result);
    $groupid=$row2['gid'];
    
    $mysql_qry2 = "insert into groups_users(group_id,user_id) values (?,?)";
    // $result1 = mysqli_query($conn ,$mysql_qry2);
    $result1 = mysqli_prepare($conn ,$mysql_qry2);
    mysqli_stmt_bind_param($result1,'ss',$groupid,$user_id);
    mysqli_stmt_execute($result1);
    $result1= mysqli_stmt_get_result($result1);
    
    $mysql_qry3 = "insert into groups_users(group_id,user_id) values (?,?)";
    //$result2 = mysqli_query($conn ,$mysql_qry3);
    $result2 = mysqli_prepare($conn ,$mysql_qry3);
    mysqli_stmt_bind_param($result2,'ss',$groupid,$friend_id);
    mysqli_stmt_execute($result2);
    $result2= mysqli_stmt_get_result($result2);
    $ret['status']="success";
    
    
}
else{
    $ret['status']="fail";
    $ret['message']="Create group fail";
}

echo json_encode($ret);

?>