<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"/>
    <meta name="ProgId" content="Excel.Sheet"/>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title></title>
</head>
<body>

<div id="1">
    <h1 align="center">New Down Networks</h1>

    <table title="New Down Networks" width="100%" border="1">
        <tr>
            <th width="100/6" style="background-color:rgb(230,0,0)"></th>
            <th width="100/6">Organization Name</th>
            <th width="100/6">Network Name</th>
            <th width="100/6">Mac Address</th>
            <th width="100/6">Device Number</th>
            <th width="100/6">Reported Down Time</th>
        </tr>
    <#list newDownNetworks as newDownNetwork>
        <tr>
            <td width="100/6" style="background-color:rgb(230,0,0)"></td>
            <td width="100/6"> ${newDownNetwork.getOrganizationName()}</td>
            <td width="100/6">${newDownNetwork.getNetworkName()}</td>
            <td width="100/6">${newDownNetwork.getMacAddress()}</td>
            <td width="100/6">${newDownNetwork.getDeviceNumber()}</td>
            <td width="100/6">${newDownNetwork.getReportedDownTime()}</td>
        </tr>
    </#list>
    </table>
</div>

<div id="2">
    <h1 align="center">Old Down Networks</h1>
    <table title="Old Down Networks" width="100%" border="1">
        <tr>
            <th width="100/6" style="background-color:rgb(255,240,0)"></th>
            <th width="100/6">Organization Name</th>
            <th width="100/6">Network Name</th>
            <th width="100/6">Mac Address</th>
            <th width="100/6">Device Number</th>
            <th width="100/6">Reported Down Time</th>
        </tr>
    <#list oldDownNetworks as oldDownNetwork>
        <tr>
            <td width="100/6" style="background-color:rgb(255,240,0)"></td>
            <td width="100/6"> ${oldDownNetwork.getOrganizationName()}</td>
            <td width="100/6">${oldDownNetwork.getNetworkName()}</td>
            <td width="100/6">${oldDownNetwork.getMacAddress()}</td>
            <td width="100/6">${oldDownNetwork.getDeviceNumber()}</td>
            <td width="100/6">${oldDownNetwork.getReportedDownTime()}</td>
        </tr>
    </#list>
    </table>
</div>

<div id="3">
    <h1 align="center">Networks Up This Cycle</h1>
    <table title="Networks Up " width="100%" border="1">
        <tr>
            <th width="100/7" style="background-color:rgb(0,150,0)"></th>
            <th width="100/7">Organization Name</th>
            <th width="100/7">Network Name</th>
            <th width="100/7">Mac Address</th>
            <th width="100/7">Device Number</th>
            <th width="100/7">Reported Down Time</th>
            <th width="100/7">Reported Up Time</th>
        </tr>
    <#list upNetworks as upNetwork>
        <tr>
            <td width="100/7" style="background-color:rgb(0,150,0)"></td>
            <td width="100/7"> ${upNetwork.getOrganizationName()}</td>
            <td width="100/7">${upNetwork.getNetworkName()}</td>
            <td width="100/7">${upNetwork.getMacAddress()}</td>
            <td width="100/7">${upNetwork.getDeviceNumber()}</td>
            <td width="100/7">${upNetwork.getReportedDownTime()}</td>
            <td width="100/7">${upNetwork.getReportedUpTime()}</td>
        </tr>
    </#list>
    </table>
</div>
</body>
</html>