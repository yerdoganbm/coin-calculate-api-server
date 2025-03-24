


/* eslint-disable react/no-access-state-in-setstate */
/* eslint-disable no-var */
/* eslint-disable block-scoped-var */
/* eslint-disable no-param-reassign */
/* eslint-disable no-plusplus */
/* eslint-disable vars-on-top */
/* eslint-disable no-redeclare */
/* eslint-disable react/no-is-mounted */
/* eslint-disable no-unused-vars */
import React from 'react';
import PropTypes from 'prop-types';
import moment from 'moment';
import injectSaga from 'utils/injectSaga';
import injectReducer from 'utils/injectReducer';
import { injectIntl } from 'react-intl';
import { connect } from 'react-redux';
import { compose } from 'redux';
import { toast } from 'react-toastify';
import { createStructuredSelector } from 'reselect';
import { Segment, Grid, Dimmer, Loader } from 'semantic-ui-react';
import { Form, Button, Radio, Drawer, DataTable, Dropdown, Label, Checkbox } from 'tcmb-ui-components';
import _ from 'lodash';
import saga from './redux/saga';
import reducer from './redux/reducer';
import {
  TahakkukColumns,
  TahakkukDetayColumns,
  TahakkukPaketColumns,
  BostakiTahakkukDetayColumns,
  TahakkuXmlDosyasiColumns,
} from './columns';
import makeSelectTahakkukIslemleri from './redux/selectors';
import { tcmbSubeOptions, tahakkukKararTipiOptions, tahakkukDurumOptions } from './redux/utility';
/* eslint-disable no-eval */
/* eslint-disable react/prop-types */
import {
  getKararList,
  getTahakkukPaket,
  createTahakkukPaket,
  searchTahakkuk,
  searchTahakkukDetay,
  deleteTahakkuk,
  onayTahakkuk,
  createTahakkukPaketDetay,
  createTahakkukSil,
  guncelleTahakkukPaketDetay,
  valueChangeAction,
  uploadTahakkukDosyasiAction,
  createManuelTahakkukPaket,
  onayGeriAlTahakkuk,
  getBostakiTahakkukPaket,
  assignBostakiTahakkukDetayToTahakkuk,
  createTahakkukKaldir,
  tahakkukListesiTemizle,
  searchTahakkukXmlDosyasiKararList,
  tahakkukSubeIliskilendir,
  clearLists,
  searchIhracatci,
  clearIhracatci,
  getKararByNo,
  dosyaProgressDurumunuGetir,
  tahakkukDosyaYuklemeModalReset,
  clearTahakkukIslemDurumList,
  searchIhracatciByDifferentParameters,
} from './redux/actions';
import TahakkukDosyasiYuklemeModal from '../../components/TahakkukDosyasiYuklemeModal';
import DropdownKararNo from '../../components/DropdownKararNo';

class TahakkukIslemleri extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      showDrawerTahakkukEkle: false,
      showDrawerManuelTahakkukEkle: false,
      showDrawerTahakkukDosyasiYukle: false,
      showDrawerTahakkukPaketDetayEkle: false,
      showDrawerBostakiTahakkukPaketDetayEkle: false,
      showDrawerTahakkukPaketDetayGuncelle: false,
      showDrawerSubeIliskilendir: false,
      selectedBostakiTahakkukPaketDetay: new Set(),
      aktifTahakkukPaketTuru: '',
      dateSearchKayitTarihi: '',
      cmbSearchTahakkukKararNo: '',
      cmbSearchTahakkukTur: '',
      cmbSearchTahakkukDurum: '1',
      txtSearchTahakkukBelgeNo: '',
      txtSearchTahakkukYil: '',
      secilenTahakkukPaketSatir: [],
      secilenTahakkukPaketDetaySatir: '',
      secilenTahakkukSatir: [],
      secilenTahakkuktakiKararSatir: [],
      rdIhracatciTipi: 'F',
      txtTckn: '',
      txtVkn: '',
      txtAd: '',
      subeId: '',
      chkTahakkukPaketHalindeEleAl: true,
      txtIhracatciHesapNumarasi: '',
      txtHakedisTutari: '',
      cmbSearchManuelTahakkukTur: '',
      txtBelgeNo: '',
      txtYil: new Date().getFullYear(),
      subeIdList: [],
      iliskiliSubeIdList: [],
      iliskilendirilecekTahakkukId: '',
      txtKararNo: '',
      clearKararNo: false,
      clearKararNoManuelTahakkukEkle: false,
    };
    this.handleSelectTahakkukPaket = this.handleSelectTahakkukPaket.bind(this);
  }

  render() {
    return <div>{this.renderTahakkukIslem()}</div>;
  }

  renderTahakkukIslem() {
    let hakedislerToplamiToplam = 0.0;
    this.state.secilenTahakkukSatir.forEach(secilenTahakkukSatir => {
      const tahakkuk = this.props.tahakkukIslemleri.tahakkukList.find(t => t.id === secilenTahakkukSatir);
      if (tahakkuk !== null && tahakkuk !== undefined) {
        hakedislerToplamiToplam += tahakkuk.hakedisToplami;
      }
    });
    TahakkuXmlDosyasiColumns.forEach(i => {
      if (i.key === 'hakedisToplami') {
        _.set(i, 'renderFooterCell', () => (
          <p style={{ textAlign: 'center' }}>
            {hakedislerToplamiToplam
              .toFixed(2)
              .toString()
              .replace('.', ',')
              .replace(/\B(?=(\d{3})+(?!\d))/g, '.')}
          </p>
        ));
      }
    });
    return (
      <div>
        {this.props.activeUserInfo !== undefined &&
        this.props.activeUserInfo.user !== undefined &&
        this.props.activeUserInfo.user.kullaniciIslemYetki !== 'OGM'
          ? null
          : this.renderTahakkukCetvelFields()}
        {this.renderSearchTahakkukFields()}
        {this.state.chkTahakkukPaketHalindeEleAl === false ? this.renderTahakkukTable() : this.renderTahakkukTableForXmlDosyasi()}
        {this.renderTahakkukDetayTable()}
      </div>
    );
  }

  renderTahakkukMainTable = (tableHeader, actionButtons) => (
    <DataTable
      columns={TahakkuXmlDosyasiColumns}
      getRowKey="id"
      header={<b className="header-segment">{tableHeader}</b>}
      rowSelection={
        this.props.activeUserInfo.user !== undefined && this.props.activeUserInfo.user.kullaniciIslemYetki === 'OGM' ? 'multiple' : ''
      }
      data={this.props.tahakkukIslemleri.tahakkukList === undefined ? [] : this.props.tahakkukIslemleri.tahakkukList}
      selectedRows={this.state.secilenTahakkukSatir}
      celled
      selectable
      noResultsMessage="Aradığınız kriterlere uygun kayıt bulunamadı"
      columnMenu
      page
      pagination
      onRowsSelect={rowsData => {
        if (rowsData.length > 0) {
          this.handleSelectTahakkukPaketler(rowsData);
        } else {
          this.handleClearTahakkukPaketlerList();
        }
      }}
      onRowSelect={(rowData, checked) => {
        if (checked) {
          const { secilenTahakkukSatir } = this.state;
          secilenTahakkukSatir.push(rowData.id);
          this.setState({ secilenTahakkukSatir });
        }
      }}
      loading={this.props.tahakkukIslemleri.tahakkukListLoading}
      onPageSizeChange={this.handleTahakkukDosyasiPageSizeChange}
      paginationProps={{
        totalPages: this.props.tahakkukIslemleri.totalPagesForTahakkuk,
        activePage: this.props.tahakkukIslemleri.activePageForTahakkuk,
        onPageChange: this.handleTahakkukDosyasiListesiPaginationChange,
      }}
      footer={
        this.props.activeUserInfo !== undefined &&
        this.props.activeUserInfo.user !== undefined &&
        this.props.activeUserInfo.user.kullaniciIslemYetki !== 'OGM'
          ? null
          : actionButtons(this)
      }
    />
  );

  setInitialValues() {
    const d = new Date();
    const year = d.getFullYear();
    this.setState({ txtSearchTahakkukYil: year });
  }

  componentDidMount() {
    this.props.dispatch(getKararList());
    this.props.dispatch(clearLists());
    this.setInitialValues();
  }

  componentDidUpdate(prevProps) {
    if (prevProps.tahakkukIslemleri.tableShouldUpdate !== this.props.tahakkukIslemleri.tableShouldUpdate) {
      this.checkTahakkukEkleShowDrawer();
      this.checkManuelTahakkukEkleShowDrawer();
      // this.checkTahakkukDosyasiYukleShowDrawer();
      this.checkTahakkukPaketDetayEkleShowDrawer();
      this.checkBostakiTahakkukPaketDetayEkleShowDrawer();
      this.checkTahakkukPaketDetayGuncelleShowDrawer();
      this.checkSubeIliskilendirShowDrawer();
      // this.searchTahakkuk(1, 10);
    }
  }

  checkTahakkukEkleShowDrawer() {
    if (this.state.showDrawerTahakkukEkle) {
      this.setState({ showDrawerTahakkukEkle: false });
    }
  }

  checkManuelTahakkukEkleShowDrawer() {
    if (this.state.showDrawerManuelTahakkukEkle) {
      this.setState({ showDrawerManuelTahakkukEkle: false });
    }
  }

  checkTahakkukPaketDetayEkleShowDrawer() {
    if (this.state.showDrawerTahakkukPaketDetayEkle) {
      this.setState({ showDrawerTahakkukPaketDetayEkle: false });
    }
  }

  checkBostakiTahakkukPaketDetayEkleShowDrawer() {
    if (this.state.showDrawerBostakiTahakkukPaketDetayEkle) {
      this.setState({ showDrawerBostakiTahakkukPaketDetayEkle: false });
    }
  }

  checkTahakkukPaketDetayGuncelleShowDrawer() {
    if (this.state.showDrawerTahakkukPaketDetayGuncelle) {
      this.setState({ showDrawerTahakkukPaketDetayGuncelle: false });
    }
  }

  checkSubeIliskilendirShowDrawer() {
    if (this.state.showDrawerSubeIliskilendir) {
      this.setState({ showDrawerSubeIliskilendir: false });
    }
  }

  onKararSelect = (kararNo, kararTipi) => {
    this.setState({ txtKararNo: kararNo, cmbSearchManuelTahakkukTur: kararTipi });
  };

  renderTahakkukCetvelFields = () => (
    <Segment.Group className="tcmb-datatable">
      <Segment className="header-segment">
        <b>Tahakkuk Girişi</b>
      </Segment>
      <Segment className="table-segment">
        <div className="align-form-fields">
          <Form>
            <Form.Group>
              <Button className="dfif-button-green" content="Tahakkuk Ekle (ws)" onClick={this.onTahakkukEkleDawerClick} />
              <Button className="dfif-button-green" content="Tahakkuk Dosyası Yükle" onClick={this.onTahakkukDosyasiYukleDawerClick} />
              <Button className="dfif-button-green" content="Manuel Tahakkuk Oluştur" onClick={this.onManuelTahakkukEkleDawerClick} />
              <Button className="dfif-button-green" content="Iade Tahakkuk Oluştur" onClick={this.onManuelTahakkukEkleDawerClick} />
            </Form.Group>
          </Form>
        </div>
      </Segment>
      <br />

      <Drawer size="600px" handler={false} open={this.state.showDrawerTahakkukEkle} onClose={this.onTahakkukEkleDawerClick} direction="top">
        <Drawer.Header>Tahakkuk Ekle</Drawer.Header>
        <Drawer.Content>
          <div>
            <Dimmer.Dimmable as={Segment}>
              <Dimmer active={this.props.tahakkukIslemleri.tahakkukPaketLoading}>
                <Loader>Yükleniyor...</Loader>
              </Dimmer>
              <DataTable
                loading={this.props.tahakkukIslemleri.tahakkukPaketGetirLoading}
                height="350px"
                columns={TahakkukPaketColumns}
                getRowKey="paketId"
                header={<b className="header-segment">Ticaret Bakanlığı Aktif Tahakkuk Paketleri</b>}
                rowSelection={
                  this.props.activeUserInfo.user !== undefined && this.props.activeUserInfo.user.kullaniciIslemYetki === 'OGM'
                    ? 'multiple'
                    : ''
                }
                data={this.props.tahakkukIslemleri.tahakkukPaketList === undefined ? [] : this.props.tahakkukIslemleri.tahakkukPaketList}
                onRowSelect={this.handleSelectTahakkukPaket}
                celled
                selectable
                color="#5E8CC6"
                noResultsMessage="Aradığınız kriterlere uygun kayıt bulunamadı"
                columnMenu
              />
            </Dimmer.Dimmable>
          </div>
        </Drawer.Content>
        <Drawer.Footer>
          <Segment basic textAlign="right">
            &nbsp;&nbsp;&nbsp;&nbsp;
            <div className="ui bottom right attached label" style={{ height: '50px', padding: '10', backgroundColor: 'transparent' }}>
              <Form>
                <Form.Group>
                  <Form.Field>
                    {' '}
                    <Button
                      id="BtnTahakkukPaketGetir"
                      className="dfif-button-blue"
                      content="Tahakkuk Paket Getir"
                      onClick={this.handleTahakkukPaketGetir}
                    />
                  </Form.Field>
                  <Form.Field>
                    <Button
                      id="btnCreateTahakkukPaket"
                      className="dfif-button-green"
                      content="Tahakkuk Paket Yükle"
                      onClick={() => this.handleCreateTahakkukPaket(this)}
                    />
                  </Form.Field>
                </Form.Group>
              </Form>
            </div>
          </Segment>
        </Drawer.Footer>
      </Drawer>

      <Drawer
        size="600px"
        handler={false}
        open={this.state.showDrawerManuelTahakkukEkle}
        onClose={this.onManuelTahakkukEkleDawerClick}
        direction="top">
        <Drawer.Header>Manuel Tahakkuk Ekle</Drawer.Header>
        <Drawer.Content>
          <div>
            <div className="align-form-fields">
              <Form
                onSubmit={(event, data) => {
                  if (event.nativeEvent.submitter.textContent === 'Kaydet') {
                    if (data.validateForm() === null) {
                      this.handleManuelTahakkukPaketEkle();
                    } else {
                      toast.error('Lütfen, hatalı alanları düzeltiniz!');
                    }
                  }
                }}>
                <DropdownKararNo onSelect={this.onKararSelect} required clearTrigger={this.state.clearKararNoManuelTahakkukEkle} />
                <Form.Select
                  id="TahakkukSearchTurId"
                  label="Tahakkuk Türü"
                  placeholder=""
                  value={this.state.cmbSearchManuelTahakkukTur}
                  search
                  clearable
                  onChange={(e, data) => {
                    this.setState({ cmbSearchManuelTahakkukTur: data.value });
                  }}
                  options={tahakkukKararTipiOptions}
                  disabled
                />
                <Form.Dropdown
                  label="Şube Listesi"
                  placeholder="Şubeler"
                  fluid
                  multiple
                  value={this.state.subeIdList}
                  selection
                  options={tcmbSubeOptions}
                  disabled={this.state.cmbSearchManuelTahakkukTur !== '4'}
                  onChange={(_e, { value }) => {
                    this.setState({ subeIdList: value });
                  }}
                />
                <Form.Input
                  label="Belge No"
                  type="number"
                  maxLength="6"
                  value={this.state.txtBelgeNo ? this.state.txtBelgeNo : ''}
                  onChange={(e, data) => {
                    this.setState({ txtBelgeNo: data.value });
                  }}
                  validation={{
                    rules: [{ type: 'required' }, { type: 'length', max: 6 }, { type: 'numeric' }],
                    validateOnChange: true,
                    validateOnMount: true,
                    showErrors: 'all',
                  }}
                />
                <Form.Input
                  label="Yıl"
                  type="number"
                  maxLength="4"
                  value={this.state.txtYil ? this.state.txtYil : ''}
                  onChange={(e, data) => {
                    this.setState({ txtYil: data.value });
                  }}
                  validation={{
                    rules: [{ type: 'required' }, { type: 'length', max: 4 }, { type: 'numeric' }],
                    validateOnChange: true,
                    validateOnMount: true,
                    showErrors: 'all',
                  }}
                />
                <Form.Group style={{ 'justify-content': 'flex-end' }}>
                  <Form.Field>
                    <Button id="btnManuelTahakkukKaydet" className="dfif-button-green" content="Kaydet" type="submit" />
                  </Form.Field>
                  <Form.Field>
                    <Button
                      id="btnManuelTahakkukKaydetTemizle"
                      className="dfif-button-white"
                      content="Temizle"
                      onClick={this.handleManuelTahakkukPaketTemizle}
                    />
                  </Form.Field>
                </Form.Group>
              </Form>
            </div>
          </div>
        </Drawer.Content>
      </Drawer>

      <TahakkukDosyasiYuklemeModal
        isDrawerOpen={this.state.showDrawerTahakkukDosyasiYukle}
        onFormSubmit={this.onTahakkukUploadSubmit}
        tahakkukDosyasiChange={this.tahakkukDosyasiChange}
        dosyaAdi={this.props.tahakkukIslemleri.uploadFormData.dosyaAdi}
        processFinished={this.props.tahakkukIslemleri.tahakkukDosyaProgress.dosyaYuklendi}
        percentage={this.props.tahakkukIslemleri.tahakkukDosyaProgress.percentage}
        getFileDownloadProgress={this.getTahakkukFileProgress}
        errorThrown={this.props.tahakkukIslemleri.tahakkukDosyaProgress.hataAlindi}
        errorMessage={this.props.tahakkukIslemleri.tahakkukDosyaProgress.hataMesaj}
      />

      <Drawer
        size="600px"
        handler={false}
        open={this.state.showDrawerSubeIliskilendir}
        onClose={this.onSubeIliskilendirDawerClick}
        direction="top">
        <Drawer.Header>İlişkili Şube Düzenle</Drawer.Header>
        <Drawer.Content>
          <div>
            <Form>
              <Form.Group>
                <Label>Şube Listesi</Label>
                <Dropdown
                  placeholder="Şubeler"
                  fluid
                  multiple
                  value={this.state.iliskiliSubeIdList}
                  selection
                  options={tcmbSubeOptions}
                  onChange={(_e, { value }) => {
                    this.setState({ iliskiliSubeIdList: value });
                  }}
                />
              </Form.Group>
              <Form.Group>
                <Form.Button
                  id="btnSubeIliskilendir"
                  style={{
                    float: 'right',
                    font: 'normal normal 600 14px/19px Open Sans',
                    border: '1px solid',
                    background: '#42906B',
                    color: '#FFFFFF',
                    top: '112px',
                    left: '420px',
                    width: '150px',
                    height: '40px',
                  }}
                  content="Şube İlişkilendir"
                  onClick={this.handleSubeIliskilendir}
                />
              </Form.Group>
            </Form>
          </div>
        </Drawer.Content>
      </Drawer>
    </Segment.Group>
  );

  handleSelectTahakkukPaket(rowData, checked) {
    if (rowData !== undefined) {
      const secilenTahakkukPaketSatirList = this.state.secilenTahakkukPaketSatir;
      if (checked) {
        let isFound = false;
        for (var i = 0; i < secilenTahakkukPaketSatirList.length; i++) {
          var currentPaketId = secilenTahakkukPaketSatirList[i];
          if (currentPaketId === rowData.paketId) {
            isFound = true;
            break;
          }
        }
        if (!isFound) {
          secilenTahakkukPaketSatirList.push(rowData.paketId);
        }
      } else {
        let isFound = false;
        for (var i = 0; i < secilenTahakkukPaketSatirList.length; i++) {
          var currentPaketId = secilenTahakkukPaketSatirList[i];
          if (currentPaketId === rowData.paketId) {
            isFound = true;
            break;
          }
        }
        if (isFound) {
          secilenTahakkukPaketSatirList.pop(rowData.paketId);
        }
      }
      this.setState({ secilenTahakkukPaketSatir: secilenTahakkukPaketSatirList });
    }
  }

  tahakkukDosyasiChange = e => {
    const dosya = e.target.files[0];
    const actionDataDosya = {
      dataKey: 'dosya',
      dataValue: dosya,
    };

    this.props.dispatch(valueChangeAction(actionDataDosya));

    const dosyaIsmi = e.target.files[0].name;
    const actionDataDoayaAdi = {
      dataKey: 'dosyaAdi',
      dataValue: dosyaIsmi,
    };
    this.props.dispatch(valueChangeAction(actionDataDoayaAdi));
  };

  onTahakkukUploadSubmit = e => {
    e.preventDefault(); // Stop form submit

    const { dosya } = this.props.tahakkukIslemleri.uploadFormData;
    const { dosyaAdi } = this.props.tahakkukIslemleri.uploadFormData;

    if (dosyaAdi === '' || dosya === '') {
      toast.error('Lütfen, bir dosya yükleyiniz');
    } else {
      const reader = new FileReader();
      reader.fileName = dosyaAdi;
      reader.me = this;
      const uploadFunc = this.uploadFile;
      reader.onload = (function() {
        return function(event) {
          const content = event.target.result;
          const fileContentArr = content.split(',');
          const fileContent = fileContentArr[1];
          const { fileName } = event.target;
          const { me } = event.target;
          uploadFunc(fileName, fileContent, dosya.type, me);
        };
      })(dosya);
      reader.readAsDataURL(dosya);
    }
  };

  getTahakkukFileProgress = () => {
    if (this.props.tahakkukIslemleri.tahakkukDosyaId != null) {
      this.props.dispatch(dosyaProgressDurumunuGetir(this.props.tahakkukIslemleri.tahakkukDosyaId));
    }
  };

  uploadFile(fileName, fileContent, fileType, me) {
    const actionData = {
      dosyaIcerik: fileContent,
      dosyaAdi: fileName,
      dosyaTuru: fileType,
    };
    me.props.dispatch(uploadTahakkukDosyasiAction(actionData));
  }

  handleKararGetir = () => {
    this.props.dispatch(getKararByNo(this.state.txtKararNo));
    if (this.props.tahakkukIslemleri.karar !== null) {
      this.setState({ cmbSearchManuelTahakkukTur: this.props.tahakkukIslemleri.karar.tip });
    }
  };

  handleTahakkukPaketGetir = () => {
    this.props.dispatch(getTahakkukPaket());
  };

  handleCreateTahakkukPaket = component => {
    if (component === undefined) {
      component = this;
    }
    if (component.state.secilenTahakkukPaketSatir.length !== 0) {
      component.props.dispatch(createTahakkukPaket(component.state.secilenTahakkukPaketSatir));
    } else {
      toast.error('Lütfen yüklenecek paket seçiniz!');
    }
  };

  renderSearchTahakkukFields = () => (
    <Segment.Group className="tcmb-datatable">
      <Segment className="header-segment">
        <b>Tahakkuk Arama</b>
      </Segment>
      <Segment className="table-segment" />
      <br />
      <div className="align-form-fields">
        <Form
          onSubmit={(event, data) => {
            if (event.nativeEvent.submitter.textContent === 'Ara') {
              if (data.validateForm() === null) {
                this.handleSearchTahakkukFields();
              } else {
                toast.error('Lütfen, formdaki hatalı alanları düzeltiniz!');
              }
            }
          }}>
          <Grid>
            <Grid.Row>
              <Grid.Column width={4}>
                <Form.Select
                  id="TahakkukSearchTurId"
                  label="Tahakkuk Türü"
                  placeholder=""
                  value={this.state.cmbSearchTahakkukTur}
                  search
                  clearable
                  onChange={(e, data) => {
                    this.setState({ cmbSearchTahakkukTur: data.value });
                  }}
                  options={tahakkukKararTipiOptions}
                />
              </Grid.Column>
              <Grid.Column width={4}>
                <Form.Input
                  label="Belge No"
                  value={this.state.txtSearchTahakkukBelgeNo ? this.state.txtSearchTahakkukBelgeNo : ''}
                  onChange={(e, data) => {
                    this.setState({ txtSearchTahakkukBelgeNo: data.value });
                  }}
                  validation={{
                    rules: [{ type: 'length', max: 7 }, { type: 'numeric' }],
                    validateOnChange: true,
                    validateOnMount: true,
                    showErrors: 'all',
                  }}
                />
              </Grid.Column>
              <Grid.Column width={8}>
                <Form.Input
                  label="Yil"
                  value={this.state.txtSearchTahakkukYil ? this.state.txtSearchTahakkukYil : ''}
                  onChange={(e, data) => {
                    this.setState({ txtSearchTahakkukYil: data.value });
                  }}
                  validation={{
                    rules: [{ type: 'length', max: 4 }, { type: 'numeric' }],
                    validateOnChange: true,
                    validateOnMount: true,
                    showErrors: 'all',
                  }}
                />
              </Grid.Column>
            </Grid.Row>
            <Grid.Row>
              <Grid.Column width={4}>
                <Form.Datepicker
                  id="tahakkukKayitstarihId"
                  label="Kayıt Tarihi"
                  onChange={date => this.setState({ dateSearchKayitTarihi: date })}
                  dateFormat="DD.MM.YYYY"
                  selected={this.state.dateSearchKayitTarihi}
                  selectsStart
                  showYearDropdown
                  showMonthDropdown
                  maxDate={moment()}
                />
              </Grid.Column>
              <Grid.Column width={4}>
                <DropdownKararNo
                  clearTrigger={this.state.clearKararNo}
                  onSelect={kararNo => this.setState({ cmbSearchTahakkukKararNo: kararNo })}
                />
              </Grid.Column>
              <Grid.Column width={8}>
                <Form.Select
                  id="TahakkukSearchDurumId"
                  label="Durum"
                  placeholder=""
                  value={this.state.cmbSearchTahakkukDurum}
                  search
                  clearable
                  onChange={(e, data) => {
                    this.setState({ cmbSearchTahakkukDurum: data.value });
                  }}
                  options={tahakkukDurumOptions}
                />
              </Grid.Column>
            </Grid.Row>
            <Grid.Row>
              <Grid.Column width={16}>
                <Checkbox
                  id="tahakkukPaketHalindeEleAl"
                  label="Tahakkukları Paket Dosyası Halinde Ele Al"
                  checked={this.state.chkTahakkukPaketHalindeEleAl}
                  onChange={this.handleTahakkukPaketHalindeEleAlChange}
                />
              </Grid.Column>
            </Grid.Row>
            <div className="align-buttons">
              <Grid.Row>
                <Form.Group>
                  <Form.Field>
                    <Button
                      id="BtnClearSearchTahakkuk"
                      className="dfif-button-white"
                      content="Temizle"
                      onClick={this.handleClearSearchTahakkukFields}
                    />
                  </Form.Field>
                  <Form.Field>
                    <Button type="submit" id="btnSearchTahakkuk" className="dfif-button-blue" content="Ara" />
                  </Form.Field>
                </Form.Group>
              </Grid.Row>
            </div>
          </Grid>
        </Form>
      </div>
    </Segment.Group>
  );

  handleTahakkukPaketHalindeEleAlChange = (e, data) => {
    this.setState({ chkTahakkukPaketHalindeEleAl: data.checked });
    this.setState({ secilenTahakkukSatir: [] });
    this.props.dispatch(clearLists());
    this.props.dispatch(tahakkukListesiTemizle());
  };

  handleInsertTahakkukPaket = (rowData, checked) => {
    if (checked) {
      const { secilenTahakkukSatir } = this.state;
      secilenTahakkukSatir.push(rowData.id);
      this.setState({ secilenTahakkukSatir });
    }
  };

  handleSelectTahakkukPaketler(rowsData) {
    const secilenTahakkukSatir = [];

    // eslint-disable-next-line array-callback-return
    rowsData.map(rowData => {
      secilenTahakkukSatir.push(rowData.id);
    });

    this.setState({ secilenTahakkukSatir });
  }

  handleClearTahakkukPaketlerList = () => {
    const secilenTahakkukSatir = [];
    this.setState({ secilenTahakkukSatir });
  };

  handleTahakkukDosyasiPageSizeChange = (event, data) => {
    const newPageSize = data.value;
    const newTotalPages = Math.ceil(this.props.tahakkukIslemleri.sizeForTahakkuk / newPageSize);
    const newActivePage = Math.min(newTotalPages, this.props.tahakkukIslemleri.activePageForTahakkuk);

    this.props.tahakkukIslemleri.rowCountForTahakkuk = newPageSize;
    this.props.tahakkukIslemleri.totalPagesForTahakkuk = newTotalPages;
    this.props.tahakkukIslemleri.activePageForTahakkuk = newActivePage;

    this.handleSearchTahakkukFields();
  };

  renderTahakkukTableForXmlDosyasi = () => (
    <div>
      {this.renderTahakkukMainTable('Tahakkuk Dosyası', this.getTahakkukXmlDosyasiActionsButtons)}
      <DataTable
        columns={TahakkukColumns}
        getRowKey="id"
        header={<b className="header-segment">Tahakkuk Dosyasındaki Tahakkuk Listesi</b>}
        rowSelection={
          this.props.activeUserInfo.user !== undefined && this.props.activeUserInfo.user.kullaniciIslemYetki === 'OGM' ? 'single' : ''
        }
        data={this.props.tahakkukIslemleri.tahakkuktakiKararList === undefined ? [] : this.props.tahakkukIslemleri.tahakkuktakiKararList}
        selectedRows={this.state.secilenTahakkuktakiKararSatir}
        celled
        selectable
        color="#5E8CC6"
        noResultsMessage="Aradığınız kriterlere uygun kayıt bulunamadı"
        columnMenu
        page
        pagination
        onRowClick={rowData => {
          const { secilenTahakkuktakiKararSatir } = this.state;
          this.setState.secilenTahakkuktakiKararSatir = [];
          secilenTahakkuktakiKararSatir.push(rowData.id);
          this.setState({ secilenTahakkuktakiKararSatir });
        }}
        onRowSelect={(rowData, checked) => {
          if (checked) {
            const secilenTahakkuktakiKararSatir = [];
            secilenTahakkuktakiKararSatir.push(rowData.id);
            this.setState({ secilenTahakkuktakiKararSatir });
          }
        }}
        loading={this.props.activeUserInfo.user !== undefined && this.props.activeUserInfo.user.kullaniciIslemYetki === undefined}
        onPageSizeChange={this.handleTahakkukDosyasiKararListesiPageSizeChange}
        paginationProps={{
          totalPages: this.props.tahakkukIslemleri.totalPagesForTahakkuk,
          activePage: this.props.tahakkukIslemleri.activePageForTahakkuk,
          onPageChange: this.handleTahakkukKararListesiPaginationChange,
        }}
        footer={
          this.props.activeUserInfo !== undefined &&
          this.props.activeUserInfo.user !== undefined &&
          this.props.activeUserInfo.user.kullaniciIslemYetki !== 'OGM'
            ? null
            : this.getTahakkukXmlDosyasiKararListesiActionsButtons()
        }
      />
    </div>
  );

  renderTahakkukTable = () => (
    <div>
      {this.renderTahakkukMainTable('Tahakkuk Paket', this.getTahakkukActionsButtons)}
      <Drawer
        className="modal-close"
        size="600px"
        handler={false}
        open={this.state.showDrawerTahakkukPaketDetayEkle}
        onClose={this.onTahakkukPaketDetayEkleDawerClick}
        direction="right">
        <Drawer.Header>Tahakkuk Paket Detay Ekle</Drawer.Header>
        <Drawer.Content>
          <Form>
            <Form.Dropdown
              label="Şube"
              placeholder="Şube"
              fluid
              value={this.state.subeId}
              selection
              options={tcmbSubeOptions}
              disabled={this.state.aktifTahakkukPaketTuru !== '4'}
              onChange={(_e, { value }) => {
                this.setState({ subeId: value });
                const { txtTckn } = this.state;
                const { txtVkn } = this.state;
                this.handleSearchIhracatciFields(txtVkn, txtTckn, value);
              }}
            />
            <label>{' İhracatçı Tipi'}</label>
            <Form.Group>
              &nbsp;&nbsp;
              <Radio
                label="Üretici"
                name="ihracatciTipi"
                value="U"
                checked={this.state.rdIhracatciTipi === 'U'}
                onChange={this.handleIhracatciTipiChange}
              />
              &nbsp;&nbsp;
              <Radio
                label="Firma"
                name="ihracatciTipi"
                value="F"
                checked={this.state.rdIhracatciTipi === 'F'}
                onChange={this.handleIhracatciTipiChange}
              />
            </Form.Group>
            <label>{' İhracatçı Tckn / Vkn'}</label>
            <Form.Group>
              <Form.Input
                id="tckn"
                type="text"
                placeholder="TC Kimlik Numarası"
                maxLength="11"
                value={this.state.txtTckn ? this.state.txtTckn : ''}
                onChange={this.handleTcknChange}
                disabled={this.state.rdIhracatciTipi === 'F'}
              />
              <Form.Input
                id="vkn"
                type="text"
                placeholder="Vergi Numarası"
                maxLength="10"
                value={this.state.txtVkn ? this.state.txtVkn : ''}
                onChange={this.handleVknChange}
                disabled={this.state.rdIhracatciTipi === 'U'}
              />
            </Form.Group>
            <Form.Input
              id="searchIhracatciAdi"
              label="İhracatçı Adı"
              type="text"
              value={this.props.tahakkukIslemleri.ihracatci.ad ? this.props.tahakkukIslemleri.ihracatci.ad : ''}
            />
            <Form.Group>
              <Form.Input
                label="Hesap Numarası"
                type="text"
                maxLength="26"
                value={this.state.txtIhracatciHesapNumarasi ? this.state.txtIhracatciHesapNumarasi : ''}
                width={8}
                onChange={(e, data) => {
                  this.setState({ txtIhracatciHesapNumarasi: data.value });
                }}
              />
            </Form.Group>
            <Form.Group>
              <Form.Input
                label="Hakediş Tutarı"
                type="number"
                maxLength="26"
                value={this.state.txtHakedisTutari ? this.state.txtHakedisTutari : ''}
                onChange={(e, data) => {
                  this.setState({ txtHakedisTutari: data.value });
                }}
              />
            </Form.Group>
          </Form>
        </Drawer.Content>
        <Drawer.Footer>
          <Segment basic textAlign="right">
            &nbsp;&nbsp;&nbsp;&nbsp;
            <div className="ui bottom right attached label" style={{ height: '50px', padding: '10', backgroundColor: 'transparent' }}>
              <Form>
                <Form.Group>
                  <Form.Button
                    id="btnTahakkukPaketDetayKaydet"
                    style={{
                      float: 'right',
                      font: 'normal normal 600 14px/19px Open Sans',
                      border: '1px solid',
                      background: '#42906B',
                      color: '#FFFFFF',
                      top: '112px',
                      left: '420px',
                      width: '140px',
                      height: '50px',
                    }}
                    content="Paket Detay Kaydet"
                    onClick={this.handleManuelTahakkukPaketDetayEkle}
                  />
                  <Form.Button
                    id="btnCreateTahakkukPaket"
                    style={{
                      float: 'right',
                      font: 'normal normal 600 14px/19px Open Sans',
                      background: '#5E8CC6',
                      color: '#FFFFFF',
                      top: '112px',
                      left: '420px',
                      width: '140px',
                      height: '50px',
                    }}
                    content="Temizle"
                    onClick={this.handleTemizleTahakkukPaketDetay}
                  />
                </Form.Group>
              </Form>
            </div>
          </Segment>
        </Drawer.Footer>
      </Drawer>
      <Drawer
        size="600px"
        handler={false}
        open={this.state.showDrawerBostakiTahakkukPaketDetayEkle}
        onClose={this.onBostakiTahakkukPaketDetayEkleDawerClick}
        direction="top">
        <Drawer.Header>Boştaki Tahakkuk Paket Detay Ekle</Drawer.Header>
        <Drawer.Content>
          <DataTable
            columns={BostakiTahakkukDetayColumns}
            getRowKey="id"
            rowSelection="multiple"
            data={
              this.props.tahakkukIslemleri.bostakiTahakkukPaketDetayList === undefined
                ? []
                : this.props.tahakkukIslemleri.bostakiTahakkukPaketDetayList
            }
            celled
            selectedRows={[]}
            selectable
            noResultsMessage="Aradığınız kriterlere uygun kayıt bulunamadı"
            columnMenu
            onRowSelect={(rowData, checked) => {
              if (rowData !== undefined) {
                const { selectedBostakiTahakkukPaketDetay } = this.state;
                if (checked) {
                  selectedBostakiTahakkukPaketDetay.add(rowData.id);
                } else {
                  selectedBostakiTahakkukPaketDetay.delete(rowData.id);
                }
                this.setState({ selectedBostakiTahakkukPaketDetay });
              }
            }}
            allRowsSelection
            onRowClick={(rowData, cellData) => {
              if (cellData.key !== undefined && cellData.props.children !== undefined) {
                this.onDawerBostakiTahakkukPaketiEklemeEkraniClick(rowData);
              }
            }}
            footer={this.getBostakiTahakkukPaketiEklemeEkraniActionsButtons()}
          />
        </Drawer.Content>
        <Drawer.Footer></Drawer.Footer>
      </Drawer>
      <Drawer
        size="600px"
        handler={false}
        open={this.state.showDrawerTahakkukPaketDetayGuncelle}
        onClose={this.onTahakkukPaketDetayGuncelleDawerClick}
        direction="left">
        <Drawer.Header>Tahakkuk Paket Detay Güncelle</Drawer.Header>
        <Drawer.Content>
          <Form>
            <Form.Dropdown
              label="Şube"
              placeholder="Şube"
              fluid
              value={this.state.subeId}
              selection
              options={tcmbSubeOptions}
              disabled={this.state.aktifTahakkukPaketTuru !== '4'}
              onChange={(_e, { value }) => {
                this.setState({ subeId: value });
                const { txtTckn } = this.state;
                const { txtVkn } = this.state;
                this.handleSearchIhracatciFields(txtVkn, txtTckn, value);
              }}
            />
            <label>{' İhracatçı Tipi'}</label>
            <Form.Group>
              &nbsp;&nbsp;
              <Radio
                label="Üretici"
                name="ihracatciTipi"
                value="U"
                checked={this.state.rdIhracatciTipi === 'U'}
                onChange={this.handleIhracatciTipiChange}
              />
              &nbsp;&nbsp;
              <Radio
                label="Firma"
                name="ihracatciTipi"
                value="F"
                checked={this.state.rdIhracatciTipi === 'F'}
                onChange={this.handleIhracatciTipiChange}
              />
            </Form.Group>
            <label>{' İhracatçı Tckn / Vkn'}</label>
            <Form.Group>
              <Form.Input
                id="tckn"
                type="text"
                placeholder="TC Kimlik Numarası"
                maxLength="11"
                value={this.state.txtTckn ? this.state.txtTckn : ''}
                onChange={this.handleTcknChange}
                disabled={this.state.rdIhracatciTipi === 'F'}
              />
              <Form.Input
                id="vkn"
                type="text"
                placeholder="Vergi Numarası"
                maxLength="10"
                value={this.state.txtVkn ? this.state.txtVkn : ''}
                onChange={this.handleVknChange}
                disabled={this.state.rdIhracatciTipi === 'U'}
              />
            </Form.Group>
            <label>{' İhracatçı Adı'}</label>
            <Form.Input
              id="ad"
              type="text"
              placeholder="İhracatçı Adı"
              value={this.state.txtAd ? this.state.txtAd : ''}
              onChange={(e, data) => {
                this.setState({ txtAd: data.value });
              }}
            />
            <Form.Group>
              <Form.Input
                label="Hesap Numarası"
                type="text"
                maxLength="26"
                width={8}
                value={this.state.txtIhracatciHesapNumarasi ? this.state.txtIhracatciHesapNumarasi : ''}
                onChange={(e, data) => {
                  this.setState({ txtIhracatciHesapNumarasi: data.value });
                }}
              />
            </Form.Group>
            <Form.Group>
              <Form.Input
                label="Hakediş Tutarı"
                type="number"
                maxLength="26"
                value={this.state.txtHakedisTutari ? this.state.txtHakedisTutari : ''}
                onChange={(e, data) => {
                  this.setState({ txtHakedisTutari: data.value });
                }}
              />
            </Form.Group>
          </Form>
        </Drawer.Content>
        <Drawer.Footer>
          <Segment basic textAlign="right">
            &nbsp;&nbsp;&nbsp;&nbsp;
            <div className="ui bottom right attached label" style={{ height: '50px', padding: '10', backgroundColor: 'transparent' }}>
              <Form>
                <Form.Group>
                  <Form.Button
                    id="btnTahakkukPaketDetayKaydet"
                    style={{
                      float: 'right',
                      font: 'normal normal 600 14px/19px Open Sans',
                      border: '1px solid',
                      background: '#42906B',
                      color: '#FFFFFF',
                      top: '112px',
                      left: '420px',
                      width: '140px',
                      height: '50px',
                    }}
                    content="Paket Detay Güncelle"
                    onClick={this.handleManuelTahakkukPaketDetayGuncelle}
                  />
                </Form.Group>
              </Form>
            </div>
          </Segment>
        </Drawer.Footer>
      </Drawer>
    </div>
  );

  getBostakiTahakkukPaketiEklemeEkraniActionsButtons() {
    return (
      <div>
        <Button
          style={{
            font: 'normal normal 600 14px/19px Open Sans',
            fontFamily: 'Helvetica, Arial, Verdana, Tahoma, sans-serif',
            background: 'green',
            color: '#FFFFFF',
            width: '180px',
            height: '35px',
          }}
          onClick={() => this.handleBostakiTahakkukDetaylariniTahakkukIleEslestir()}>
          Ekle
        </Button>
      </div>
    );
  }

  handleBostakiTahakkukDetaylariniTahakkukIleEslestir = () => {
    if (this.state.selectedBostakiTahakkukPaketDetay.size > 0) {
      this.props.dispatch(
        assignBostakiTahakkukDetayToTahakkuk(this.state.selectedBostakiTahakkukPaketDetay, this.state.secilenTahakkukSatir[0]),
      );
      this.state.selectedBostakiTahakkukPaketDetay.clear();
      this.setState({ selectedBostakiTahakkukPaketDetay: new Set() });
      this.checkBostakiTahakkukPaketDetayEkleShowDrawer();
    } else {
      toast.warn('Lütfen İlgili Tahakkuk ile İlişkilendirilecek En Az 1 Tahakkuk Detayı Seçiniz!');
    }
  };

  handleSubeIliskilendir = () => {
    this.props.dispatch(tahakkukSubeIliskilendir(this.state.iliskilendirilecekTahakkukId, this.state.iliskiliSubeIdList));
    this.onSubeIliskilendirDawerClick();
  };

  handleIhracatciTipiChange = (e, data) => {
    this.setState({ txtTckn: '' });
    this.setState({ txtVkn: '' });
    this.setState({ rdIhracatciTipi: data.value });
  };

  handleTemizleTahakkukPaketDetay = () => {
    this.props.dispatch(clearIhracatci());
    this.setState({
      rdIhracatciTipi: 'F',
      txtTckn: '',
      txtVkn: '',
      txtAd: '',
      txtIhracatciHesapNumarasi: '',
      txtHakedisTutari: '',
    });
  };

  handleManuelTahakkukPaketTemizle = () => {
    this.setState({
      txtKararNo: '',
      subeIdList: [],
      cmbSearchManuelTahakkukTur: '',
      txtBelgeNo: '',
      txtYil: new Date().getFullYear(),
      clearKararNoManuelTahakkukEkle: !this.state.clearKararNoManuelTahakkukEkle,
    });
  };

  handleManuelTahakkukPaketEkle = () => {
    this.props.dispatch(
      createManuelTahakkukPaket(
        this.state.txtYil,
        this.state.txtBelgeNo,
        this.state.cmbSearchManuelTahakkukTur,
        this.state.txtKararNo,
        this.state.subeIdList,
      ),
    );
  };

  handleManuelTahakkukPaketDetayEkle = () => {
    this.props.dispatch(
      createTahakkukPaketDetay(
        this.state.secilenTahakkukSatir[0],
        this.state.subeId,
        this.state.txtTckn,
        this.state.txtVkn,
        this.state.txtAd,
        this.state.txtIhracatciHesapNumarasi,
        this.state.txtHakedisTutari,
      ),
    );
  };

  handleManuelTahakkukPaketDetayGuncelle = () => {
    this.props.dispatch(
      guncelleTahakkukPaketDetay(
        this.state.secilenTahakkukPaketDetaySatir[0],
        this.state.subeId,
        this.state.txtTckn,
        this.state.txtVkn,
        this.state.txtAd,
        this.state.txtIhracatciHesapNumarasi,
        this.state.txtHakedisTutari,
        this.props.tahakkukIslemleri.activePageForTahakkukDetay,
        this.props.tahakkukIslemleri.rowCountForTahakkukDetay,
        this.state.secilenTahakkukSatir,
      ),
    );
  };

  handleTcknChange = e => {
    const tckn = e.target.value;
    if (tckn === '') {
      this.setState({ txtTckn: '' });
    }
    if (!Number(tckn)) {
      toast.warn('Türkiye Cumhuriyeti Kimlik Numarası alanına sadece sayı yazılabilir.');
    } else {
      this.setState({ txtTckn: e.target.value });
    }
    if (tckn.length === 11) {
      const { subeId } = this.state;
      this.handleSearchIhracatciFields('', tckn, subeId);
    } else {
      this.props.dispatch(clearIhracatci());
    }
  };

  handleVknChange = e => {
    const vkn = e.target.value;
    if (vkn === '') {
      this.setState({ txtVkn: '' });
    }
    if (!Number(vkn)) {
      toast.warn('Vergi Kimlik Numarası alanına sadece sayı yazılabilir.');
    } else {
      this.setState({ txtVkn: e.target.value });
    }
    if (vkn.length === 10) {
      const { subeId } = this.state;
      this.handleSearchIhracatciFields(vkn, '', subeId);
    } else {
      this.props.dispatch(clearIhracatci());
    }
  };

  handleSearchIhracatciFields(txtVkn, txtTckn, subeId) {
    const { aktifTahakkukPaketTuru } = this.state;
    this.props.dispatch(searchIhracatciByDifferentParameters(txtVkn, txtTckn, aktifTahakkukPaketTuru, subeId));
  }

  renderTahakkukDetayTable = () => (
    <DataTable
      columns={TahakkukDetayColumns}
      getRowKey="id"
      header={<b className="header-segment">Tahakkuk Paket Detay</b>}
      rowSelection={
        this.props.activeUserInfo.user !== undefined && this.props.activeUserInfo.user.kullaniciIslemYetki === 'OGM' ? 'single' : ''
      }
      data={this.props.tahakkukIslemleri.tahakkukDetayList === undefined ? [] : this.props.tahakkukIslemleri.tahakkukDetayList}
      selectedRows={this.state.secilenTahakkukDetaySatir}
      celled
      selectable
      color="#5E8CC6"
      noResultsMessage="Aradığınız kriterlere uygun kayıt bulunamadı"
      columnMenu
      onRowClick={rowData => {
        const { secilenTahakkukPaketDetaySatir } = this.state.secilenTahakkukPaketDetaySatir;
        this.setState.secilenTahakkukPaketDetaySatir = [];
        secilenTahakkukPaketDetaySatir.push(rowData.id);
        this.setState({ secilenTahakkukPaketDetaySatir });
      }}
      onRowSelect={(rowData, checked) => {
        if (checked) {
          const secilenTahakkukPaketDetaySatir = [];
          secilenTahakkukPaketDetaySatir.push(rowData.id);
          this.setState({ secilenTahakkukPaketDetaySatir });
        }
      }}
      loading={this.props.tahakkukIslemleri.tahakkukDetayListLoading}
      onPageSizeChange={this.handleTahakkukDetayPageSizeChange}
      paginationProps={{
        totalPages: this.props.tahakkukIslemleri.totalPagesForTahakkukDetay,
        activePage: this.props.tahakkukIslemleri.activePageForTahakkukDetay,
        onPageChange: this.handleTahakkukDetayPaginationChange.bind(this),
      }}
      footer={this.getTahakkukDetayActionsButtons()}
    />
  );

  getTahakkukPaketActionsButtons() {}

  getTahakkukActionsButtons(me) {
    return (
      <div>
        <Button className="dfif-button-blue" onClick={() => me.handleTahakkukPaketDetay()}>
          Detaylar
        </Button>
        <Button className="dfif-button-purple" onClick={() => me.handleIliskiliSubeDuzenle()}>
          İlişkili Şube Düzenle
        </Button>
        <Button className="dfif-button-cyan" onClick={() => me.handleTahakkukPaketDetayEkle()}>
          Yeni Detay Ekle
        </Button>
        <Button className="dfif-button-cyan" onClick={() => me.handleBostakiTahakkukPaketDetayEkle()}>
          Boştaki Detayı Ekle
        </Button>
        <Button className="dfif-button-green" onClick={() => me.handleTahakkukOnay()}>
          Onay Ver
        </Button>
        <Button className="dfif-button-orange" onClick={() => me.handleTahakkukOnayGeriAl()}>
          Onay Geri Al
        </Button>
        <Button className="dfif-button-red" onClick={() => me.handleTahakkukSil()}>
          Sil
        </Button>
      </div>
    );
  }

  getTahakkukXmlDosyasiKararListesiActionsButtons() {
    return (
      <div>
        <Button className="dfif-button-blue" onClick={() => this.handleTahakkukXmlDosyasiKararlariPaketDetay()}>
          Detaylar
        </Button>
      </div>
    );
  }

  getTahakkukXmlDosyasiActionsButtons = () => (
    <div>
      <Button className="dfif-button-blue" onClick={() => this.handleTahakkukXmlKararDetay()}>
        Detaylar
      </Button>
      <Button className="dfif-button-green" onClick={() => this.handleTahakkukOnay()}>
        Onay Ver
      </Button>
      <Button className="dfif-button-orange" onClick={() => this.handleTahakkukOnayGeriAl()}>
        Onay Geri Al
      </Button>
      <Button
        className="dfif-button-red"
        onClick={() => this.handleTahakkukSil()}
        loading={this.props.tahakkukIslemleri.tahakkukDosyasiSilLoading}>
        Sil
      </Button>
    </div>
  );

  getTahakkukDetayActionsButtons = () => (
    <div>
      <Button className="dfif-button-cyan" onClick={() => this.handleTahakkukDetayGuncelle()}>
        Güncelle
      </Button>
      <Button className="dfif-button-red" onClick={() => this.handleTahakkukDetayKaldır()}>
        Detayı Kaldır
      </Button>
      <Button className="dfif-button-red" onClick={() => this.handleTahakkukDetaySil()}>
        Detayı Sil
      </Button>
      &nbsp;&nbsp;&nbsp;&nbsp;
    </div>
  );

  handleTahakkukPaketDetayEkle = () => {
    if (this.state.secilenTahakkukSatir.length > 0) {
      if (this.state.secilenTahakkukSatir.length > 1) {
        toast.error('Lütfen Paket Detayı Eklemek İstediğniz 1 adet Tahakkuk Seçiniz!');
        return;
      }
      const secilenTahakkukId = this.state.secilenTahakkukSatir[0];
      const { tahakkukList } = this.props.tahakkukIslemleri;
      for (let i = 0; i < tahakkukList.length; i += 1) {
        const currentTahakkuk = tahakkukList[i];
        if (currentTahakkuk.id === secilenTahakkukId) {
          this.setState({ aktifTahakkukPaketTuru: currentTahakkuk.kararTipi });
          break;
        }
      }
      this.handleTemizleTahakkukPaketDetay();
      this.onTahakkukPaketDetayEkleDawerClick();
    } else {
      toast.error('Lütfen Paket Detayı Eklemek İstediğniz Tahakkuku Seçiniz!');
    }
  };

  handleBostakiTahakkukPaketDetayEkle = () => {
    if (this.state.secilenTahakkukSatir.length > 0) {
      if (this.state.secilenTahakkukSatir.length > 1) {
        toast.error('Lütfen Paket Detayı Eklemek İstediğiniz 1 adet Tahakkuk Seçiniz!');
        return;
      }
      const secilenTahakkukId = this.state.secilenTahakkukSatir[0];
      const { tahakkukList } = this.props.tahakkukIslemleri;
      for (let i = 0; i < tahakkukList.length; i += 1) {
        const currentTahakkuk = tahakkukList[i];
        if (currentTahakkuk.id === secilenTahakkukId) {
          break;
        }
      }
      this.onBostakiTahakkukPaketDetayEkleDawerClick();
      this.props.dispatch(getBostakiTahakkukPaket(secilenTahakkukId));
    } else {
      toast.error('Lütfen Paket Detayı Eklemek İstediğiniz Tahakkuku Seçiniz!');
    }
  };

  handleBostakiTahakkukXmlDosyasiKararlariPaketDetayEkle = () => {
    if (this.state.secilenTahakkukSatir.length > 0) {
      const secilenTahakkukId = this.state.secilenTahakkukSatir[0];
      const { tahakkukList } = this.props.tahakkukIslemleri;
      for (let i = 0; i < tahakkukList.length; i += 1) {
        const currentTahakkuk = tahakkukList[i];
        if (currentTahakkuk.id === secilenTahakkukId) {
          break;
        }
      }
      this.onBostakiTahakkukPaketDetayEkleDawerClick();
      this.props.dispatch(clearIhracatci());
      this.props.dispatch(getBostakiTahakkukPaket(secilenTahakkukId));
    } else {
      toast.error('Lütfen Paket Detayı Eklemek İstediğniz Tahakkuku Seçiniz!');
    }
  };

  handleTahakkukPaketDetay = () => {
    if (this.state.secilenTahakkukSatir.length > 0) {
      if (this.state.secilenTahakkukSatir.length > 1) {
        toast.error('Lütfen Paket Detayları Listelenecek 1 adet tahakkuk seçiniz!');
        return;
      }
      const secilenTahakkukId = this.state.secilenTahakkukSatir[0];
      const { tahakkukList } = this.props.tahakkukIslemleri;
      for (let i = 0; i < tahakkukList.length; i += 1) {
        const currentTahakkuk = tahakkukList[i];
        if (currentTahakkuk.id === secilenTahakkukId) {
          break;
        }
      }
      this.handleSearchTahakkukDetayFields();
    } else {
      toast.error('Lütfen Paket Detayları Listelenecek Tahakkuku Seçiniz!');
    }
  };

  handleIliskiliSubeDuzenle = () => {
    if (this.state.secilenTahakkukSatir.length > 0) {
      if (this.state.secilenTahakkukSatir.length > 1) {
        toast.error('Lütfen İlişkili Şubeleri Düzenlenecek 1 adet Tahakkuk Seçiniz!');
        return;
      }
      const secilenTahakkukId = this.state.secilenTahakkukSatir[0];
      const { tahakkukList } = this.props.tahakkukIslemleri;
      let secilenTahakkuk = null;
      for (let i = 0; i < tahakkukList.length; i += 1) {
        const currentTahakkuk = tahakkukList[i];
        if (currentTahakkuk.id === secilenTahakkukId) {
          secilenTahakkuk = currentTahakkuk;
          break;
        }
      }
      if (secilenTahakkuk !== null) {
        if (secilenTahakkuk.tur !== '4') {
          toast.error('Tarımsal olmayan tahakkuk paketlerinde İlişkili Şube Düzenlemesi yapılamaz.');
          return;
        }
        this.setState({ iliskiliSubeIdList: secilenTahakkuk.subeler, iliskilendirilecekTahakkukId: secilenTahakkuk.id });
        this.onSubeIliskilendirDawerClick();
      }
    } else {
      toast.error('Lütfen İlişkili Şube Düzenlenecek Tahakkuk seçiniz!');
    }
  };

  handleTahakkukXmlDosyasiKararlariPaketDetay = () => {
    if (this.state.secilenTahakkuktakiKararSatir.length > 0) {
      const secilenTahakkukId = this.state.secilenTahakkuktakiKararSatir[0];
      const { tahakkuktakiKararList } = this.props.tahakkukIslemleri;
      for (let i = 0; i < tahakkuktakiKararList.length; i += 1) {
        const currentTahakkuk = tahakkuktakiKararList[i];
        if (currentTahakkuk.id === secilenTahakkukId) {
          break;
        }
      }

      this.props.tahakkukIslemleri.actionResponseSet = new Set();
      this.searchTahakkukDetayForXMLDosyasiKararlari(1);
      // this.setState({ secilenTahakkuktakiKararSatir: '' });
    } else {
      toast.error('Lütfen Paket Detayları Listelenecek Tahakkuku Seçiniz!');
    }
  };

  handleTahakkukXmlKararDetay = () => {
    if (this.state.secilenTahakkukSatir.length > 1) {
      toast.error('Lütfen, detay görüntülemek için yalnızca 1 adet tahakkuk seçiniz!');
    } else if (this.state.secilenTahakkukSatir.length > 0) {
      const secilenTahakkukId = this.state.secilenTahakkukSatir[0];
      const { tahakkukList } = this.props.tahakkukIslemleri;
      for (let i = 0; i < tahakkukList.length; i += 1) {
        const currentTahakkuk = tahakkukList[i];
        if (currentTahakkuk.id === secilenTahakkukId) {
          break;
        }
      }
      this.handleSearchTahakkukXmlKararDetayFields();
    } else {
      toast.error('Lütfen Paket Detayları Listelenecek Tahakkuku Seçiniz!');
    }
  };

  handleTahakkukOnay = () => {
    this.props.dispatch(clearTahakkukIslemDurumList());
    let errorRaised = false;
    if (this.state.secilenTahakkukSatir.length > 0) {
      this.state.secilenTahakkukSatir.forEach(secilenTahakkukId => {
        const { tahakkukList } = this.props.tahakkukIslemleri;
        let secilenTahakkuk = null;
        for (let i = 0; i < tahakkukList.length; i += 1) {
          const currentTahakkuk = tahakkukList[i];
          if (currentTahakkuk.id === secilenTahakkukId) {
            secilenTahakkuk = currentTahakkuk;
            break;
          }
        }
        if (secilenTahakkuk !== null && secilenTahakkuk.durum !== '1' && secilenTahakkuk.durum !== '2') {
          errorRaised = true;
          toast.error('ONAY BEKLİYOR ve DÜZELT TALEBİ dışındaki durumlara sahip tahakkuk kayıtlarını onaylayamazsınız.');
        }
      });
      if (!errorRaised) {
        this.props.dispatch(
          onayTahakkuk(
            this.state.secilenTahakkukSatir,
            this.props.tahakkukIslemleri.activePageForTahakkuk,
            this.props.tahakkukIslemleri.rowCountForTahakkuk,
            this.props.tahakkukIslemleri.actionResponseSetForTahakkuk,
            this.state.dateSearchKayitTarihi,
            this.state.cmbSearchTahakkukKararNo,
            this.state.cmbSearchTahakkukTur,
            this.state.cmbSearchTahakkukDurum,
            this.state.txtSearchTahakkukBelgeNo,
            this.state.txtSearchTahakkukYil,
            this.state.chkTahakkukPaketHalindeEleAl,
          ),
        );
        this.setState({ secilenTahakkukSatir: [] });
      }
    } else {
      toast.error('Lütfen onaylanacak Tahakkuk seçiniz!');
    }
  };

  handleTahakkukOnayGeriAl = () => {
    this.props.dispatch(clearTahakkukIslemDurumList());
    let errorRaised = false;
    if (this.state.secilenTahakkukSatir.length > 0) {
      this.state.secilenTahakkukSatir.forEach(secilenTahakkukId => {
        const { tahakkukList } = this.props.tahakkukIslemleri;
        let secilenTahakkuk = null;
        for (let i = 0; i < tahakkukList.length; i += 1) {
          const currentTahakkuk = tahakkukList[i];
          if (currentTahakkuk.id === secilenTahakkukId) {
            secilenTahakkuk = currentTahakkuk;
            break;
          }
        }
        if (secilenTahakkuk !== null && secilenTahakkuk.durum !== '3') {
          errorRaised = true;
          toast.error('ONAYLANMIŞ dışındaki durumlara sahip tahakkuk kayıtlarınının onayını geri alamazsınız.');
        }
      });
      if (!errorRaised) {
        this.props.dispatch(
          onayGeriAlTahakkuk(
            this.state.secilenTahakkukSatir,
            this.props.tahakkukIslemleri.activePageForTahakkuk,
            this.props.tahakkukIslemleri.rowCountForTahakkuk,
            this.props.tahakkukIslemleri.actionResponseSetForTahakkuk,
            this.state.dateSearchKayitTarihi,
            this.state.cmbSearchTahakkukKararNo,
            this.state.cmbSearchTahakkukTur,
            this.state.cmbSearchTahakkukDurum,
            this.state.txtSearchTahakkukBelgeNo,
            this.state.txtSearchTahakkukYil,
            this.state.chkTahakkukPaketHalindeEleAl,
          ),
        );
        this.setState({ secilenTahakkukSatir: [] });
      }
    } else {
      toast.error('Lütfen onaylanacak Tahakkuk seçiniz!');
    }
  };

  handleTahakkukSil = () => {
    this.props.dispatch(clearTahakkukIslemDurumList());
    let errorRaised = false;
    if (this.state.secilenTahakkukSatir.length > 0) {
      this.state.secilenTahakkukSatir.forEach(secilenTahakkukId => {
        const { tahakkukList } = this.props.tahakkukIslemleri;
        let secilenTahakkuk = null;
        for (let i = 0; i < tahakkukList.length; i += 1) {
          const currentTahakkuk = tahakkukList[i];
          if (currentTahakkuk.id === secilenTahakkukId) {
            secilenTahakkuk = currentTahakkuk;
            break;
          }
        }
        if (secilenTahakkuk !== null && secilenTahakkuk.durum !== '1' && secilenTahakkuk.durum !== '2') {
          errorRaised = true;
          toast.error('ONAY BEKLİYOR ve DÜZELT TALEBİ dışındaki durumlara sahip tahakkuk kayıtlarını silemezsiniz.');
        }
      });
      if (!errorRaised) {
        this.props.dispatch(
          deleteTahakkuk(
            this.state.secilenTahakkukSatir,
            this.props.tahakkukIslemleri.activePageForTahakkuk,
            this.props.tahakkukIslemleri.rowCountForTahakkuk,
            this.props.tahakkukIslemleri.actionResponseSetForTahakkuk,
            this.state.dateSearchKayitTarihi,
            this.state.cmbSearchTahakkukKararNo,
            this.state.cmbSearchTahakkukTur,
            this.state.cmbSearchTahakkukDurum,
            this.state.txtSearchTahakkukBelgeNo,
            this.state.txtSearchTahakkukYil,
            this.state.chkTahakkukPaketHalindeEleAl,
          ),
        );
        this.setState({ secilenTahakkukSatir: [] });
      }
    } else {
      toast.error('Lütfen onaylanacak Tahakkuk seçiniz!');
    }
  };

  handleTahakkukDetayGuncelle = () => {
    if (this.state.secilenTahakkukPaketDetaySatir.length > 0) {
      const secilenTahakkukDetayId = this.state.secilenTahakkukPaketDetaySatir[0];
      const { tahakkukDetayList } = this.props.tahakkukIslemleri;
      let secilenTahakkuk = null;
      for (let i = 0; i < tahakkukDetayList.length; i += 1) {
        const currentTahakkukDetay = tahakkukDetayList[i];
        if (currentTahakkukDetay.id === secilenTahakkukDetayId) {
          secilenTahakkuk = currentTahakkukDetay;
          break;
        }
      }
      if (secilenTahakkuk !== null) {
        if (secilenTahakkuk.tahakkukPaketDosyasiId !== null) {
          toast.error('XML dosyasından yüklenen tahakkukun detaylarında güncelleme yapamazsınız!');
          return;
        }
        const { tahakkukList } = this.props.tahakkukIslemleri;
        for (let i = 0; i < tahakkukList.length; i += 1) {
          const currentTahakkuk = tahakkukList[i];
          if (currentTahakkuk.id === this.state.secilenTahakkukSatir[0]) {
            this.setState({ aktifTahakkukPaketTuru: currentTahakkuk.kararTipi });
            if (currentTahakkuk.kararTipi === '4') {
              this.setState({ subeId: secilenTahakkuk.subeKodu });
            }
            break;
          }
        }
        let currentRdIhracatciTipi = '';
        let currentTckn = '';
        let currentVkn = '';
        if (secilenTahakkuk.tckn !== null && secilenTahakkuk.tckn.trim() !== '') {
          currentRdIhracatciTipi = 'U';
          currentTckn = secilenTahakkuk.tckn;
        } else if (secilenTahakkuk.vkn !== null && secilenTahakkuk.vkn.trim() !== '') {
          currentRdIhracatciTipi = 'F';
          currentVkn = secilenTahakkuk.vkn;
        }
        const currentIhracatciAdi = secilenTahakkuk.ihracatciAdi;
        const currentHakedisTutari = secilenTahakkuk.hakedisTutar;
        const currentIhracatciHesapNumarasi = secilenTahakkuk.hesapNo;
        this.setState({
          rdIhracatciTipi: currentRdIhracatciTipi,
          txtTckn: currentTckn,
          txtVkn: currentVkn,
          txtAd: currentIhracatciAdi,
          txtHakedisTutari: currentHakedisTutari,
          txtIhracatciHesapNumarasi: currentIhracatciHesapNumarasi,
        });
        this.onTahakkukPaketDetayGuncelleDawerClick();
      }
    } else {
      toast.error('Lütfen Paket Detayları Listelenecek Tahakkuku Seçiniz!');
    }
  };

  handleTahakkukDetaySil = () => {
    if (this.state.secilenTahakkukPaketDetaySatir.length > 0) {
      const secilenTahakkukDetayId = this.state.secilenTahakkukPaketDetaySatir[0];
      this.props.dispatch(createTahakkukSil(secilenTahakkukDetayId));
      this.setState({ secilenTahakkukPaketDetaySatir: '' });
      this.handleTahakkukPaketDetay();
    } else {
      toast.error('Lütfen silinecek Tahakkuk Detay kaydını seçiniz!');
    }
    this.setState({});
  };

  handleTahakkukDetayKaldır = () => {
    if (this.state.secilenTahakkukPaketDetaySatir.length > 0) {
      const secilenTahakkukDetayId = this.state.secilenTahakkukPaketDetaySatir[0];
      this.props.dispatch(createTahakkukKaldir(secilenTahakkukDetayId));
      this.setState({ secilenTahakkukPaketDetaySatir: '' });
      this.handleTahakkukPaketDetay();
    } else {
      toast.error('Lütfen kaldırılacak Tahakkuk Detay kaydını seçiniz!');
    }
    this.setState({});
  };

  onTahakkukDosyasiYukleDawerClick = () => {
    this.props.dispatch(tahakkukDosyaYuklemeModalReset());
    this.setState(prevState => ({ showDrawerTahakkukDosyasiYukle: !prevState.showDrawerTahakkukDosyasiYukle }));
  };

  onManuelTahakkukEkleDawerClick = () => {
    this.handleManuelTahakkukPaketTemizle();
    this.setState(prevState => ({ showDrawerManuelTahakkukEkle: !prevState.showDrawerManuelTahakkukEkle }));
  };

  onTahakkukEkleDawerClick = () => {
    this.setState(prevState => ({ showDrawerTahakkukEkle: !prevState.showDrawerTahakkukEkle }));
    this.setState({ secilenTahakkukPaketSatir: [] });
  };

  onTahakkukPaketDetayEkleDawerClick = () => {
    this.setState(prevState => ({ showDrawerTahakkukPaketDetayEkle: !prevState.showDrawerTahakkukPaketDetayEkle }));
    this.setState({ subeId: '' });
  };

  onBostakiTahakkukPaketDetayEkleDawerClick = () => {
    this.setState(prevState => ({ showDrawerBostakiTahakkukPaketDetayEkle: !prevState.showDrawerBostakiTahakkukPaketDetayEkle }));
  };

  onTahakkukPaketDetayGuncelleDawerClick = () => {
    this.setState(prevState => ({ showDrawerTahakkukPaketDetayGuncelle: !prevState.showDrawerTahakkukPaketDetayGuncelle }));
  };

  onSubeIliskilendirDawerClick = () => {
    this.setState(prevState => ({ showDrawerSubeIliskilendir: !prevState.showDrawerSubeIliskilendir }));
  };

  handleClearSearchTahakkukFields = () => {
    this.setState({
      dateSearchKayitTarihi: '',
      cmbSearchTahakkukKararNo: '',
      cmbSearchTahakkukTur: '',
      cmbSearchTahakkukDurum: '1',
      txtSearchTahakkukBelgeNo: '',
      txtSearchTahakkukYil: '',
      clearKararNo: !this.state.clearKararNo,
    });
  };

  handleSearchTahakkukFields = () => {
    this.props.dispatch(clearTahakkukIslemDurumList());
    this.props.tahakkukIslemleri.actionResponseSet = new Set();
    this.searchTahakkuk(1);
  };

  searchTahakkuk = (activePage, rowCount, actionResponseSet) => {
    this.props.dispatch(clearLists());
    this.props.dispatch(
      searchTahakkuk(
        activePage !== undefined ? activePage : this.props.tahakkukIslemleri.activePageForTahakkuk,
        rowCount !== undefined ? rowCount : this.props.tahakkukIslemleri.rowCountForTahakkuk,
        actionResponseSet !== undefined ? actionResponseSet : this.props.tahakkukIslemleri.actionResponseSetForTahakkuk,
        this.state.dateSearchKayitTarihi,
        this.state.cmbSearchTahakkukKararNo,
        this.state.cmbSearchTahakkukTur,
        this.state.cmbSearchTahakkukDurum,
        this.state.txtSearchTahakkukBelgeNo,
        this.state.txtSearchTahakkukYil,
        this.state.chkTahakkukPaketHalindeEleAl,
      ),
    );
  };

  handleSearchTahakkukXmlKararDetayFields = () => {
    this.props.tahakkukIslemleri.actionResponseSet = new Set();
    this.searchTahakkukXmlDosyasiKararList();
  };

  handleSearchTahakkukDetayFields = () => {
    this.props.tahakkukIslemleri.actionResponseSet = new Set();
    this.searchTahakkukDetay();
  };

  handleSearchTahakkukDetayForXMLDosyasiKararlariFields = () => {
    this.props.tahakkukIslemleri.actionResponseSet = new Set();
    this.searchTahakkukDetayForXMLDosyasiKararlari();
  };

  searchTahakkukDetay = (activePage, rowCount, actionResponseSet) => {
    this.props.dispatch(
      searchTahakkukDetay(
        activePage !== undefined ? activePage : this.props.tahakkukIslemleri.activePageForTahakkukDetay,
        rowCount !== undefined ? rowCount : this.props.tahakkukIslemleri.rowCountForTahakkukDetay,
        actionResponseSet !== undefined ? actionResponseSet : this.props.tahakkukIslemleri.actionResponseSetForTahakkukDetay,
        this.state.secilenTahakkukSatir,
      ),
    );
  };

  searchTahakkukDetayForXMLDosyasiKararlari = (activePage, rowCount, actionResponseSet) => {
    this.props.dispatch(
      searchTahakkukDetay(
        activePage !== undefined ? activePage : this.props.tahakkukIslemleri.activePageForTahakkukDetay,
        rowCount !== undefined ? rowCount : this.props.tahakkukIslemleri.rowCountForTahakkukDetay,
        actionResponseSet !== undefined ? actionResponseSet : this.props.tahakkukIslemleri.actionResponseSetForTahakkukDetay,
        this.state.secilenTahakkuktakiKararSatir,
      ),
    );
  };

  searchTahakkukXmlDosyasiKararList = (activePage, rowCount, actionResponseSet) => {
    this.props.dispatch(
      searchTahakkukXmlDosyasiKararList(
        activePage !== undefined ? activePage : this.props.tahakkukIslemleri.activePageForTahakkukDetay,
        rowCount !== undefined ? rowCount : this.props.tahakkukIslemleri.rowCountForTahakkukDetay,
        actionResponseSet !== undefined ? actionResponseSet : this.props.tahakkukIslemleri.actionResponseSetForTahakkukKararList,
        this.state.secilenTahakkukSatir,
      ),
    );
  };

  handleTahakkukPaginationChange = (event, { activePage }) => {
    if (activePage !== this.props.tahakkukIslemleri.activePageForTahakkuk) {
      this.searchTahakkuk(activePage);
    }
  };

  handleTahakkukDosyasiListesiPaginationChange = (event, { activePage }) => {
    if (activePage !== this.props.tahakkukIslemleri.activePageForTahakkuk) {
      this.searchTahakkuk(activePage);
    }
  };

  handleTahakkukPageSizeChange(event, data) {
    const newPageSize = data.value;
    const newTotalPages = Math.ceil(this.props.tahakkukIslemleri.sizeForTahakkuk / newPageSize);
    const newActivePage = Math.min(newTotalPages, this.props.tahakkukIslemleri.activePageForTahakkuk);
    this.props.tahakkukIslemleri.rowCountForTahakkuk = newPageSize;
    this.props.tahakkukIslemleri.totalPagesForTahakkuk = newTotalPages;
    this.props.tahakkukIslemleri.activePageForTahakkuk = newActivePage;
    this.searchTahakkuk();
  }

  handleTahakkukDetayPaginationChange(event, { activePage }) {
    if (activePage !== this.activePage) {
      this.searchTahakkukDetayForXMLDosyasiKararlari(activePage);
    }
  }

  handleTahakkukDetayPageSizeChange(event, data) {
    const newPageSize = data.value;
    const newTotalPages = Math.ceil(this.props.tahakkukIslemleri.sizeForTahakkukDetay / newPageSize);
    const newActivePage = Math.min(newTotalPages, this.props.tahakkukIslemleri.activePageForTahakkukDetay);
    this.props.tahakkukIslemleri.rowCountForTahakkukDetay = newPageSize;
    this.props.tahakkukIslemleri.totalPagesForTahakkukDetay = newTotalPages;
    this.props.tahakkukIslemleri.activePageForTahakkukDetay = newActivePage;
    this.searchTahakkukDetay();
  }
}

TahakkukIslemleri.propTypes = {
  dispatch: PropTypes.func.isRequired,
  tahakkukIslemleri: PropTypes.any,
  activeUserInfo: PropTypes.any,
};

function mapDispatchToProps(dispatch) {
  return {
    dispatch,
  };
}

const mapStateToProps = createStructuredSelector({
  tahakkukIslemleri: makeSelectTahakkukIslemleri(),
});

const withConnect = connect(mapStateToProps, mapDispatchToProps);

const withReducer = injectReducer({ key: 'tahakkukIslemleri', reducer });
const withSaga = injectSaga({ key: 'tahakkukIslemleri', saga });

export default compose(withReducer, withSaga, withConnect)(injectIntl(TahakkukIslemleri));
