import { Component, OnInit, OnDestroy } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { Router } from '@angular/router';
import { Campaign, CampaignExecutionReport } from '@core/model';
import { CampaignService } from '@core/services';
import { Subscription, timer } from 'rxjs';

@Component({
    selector: 'chutney-campaigns',
    templateUrl: './campaign-list.component.html',
    styleUrls: ['./campaign-list.component.scss']
})
export class CampaignListComponent implements OnInit, OnDestroy {

    deletionConfirmationTextPrefix: string;
    deletionConfirmationTextSuffix: string;

    campaigns: Array<Campaign> = [];
    lastCampaignReports: Array<CampaignExecutionReport> = [];
    lastCampaignReportsSub: Subscription;
    campaignFilter: string;

    constructor(private campaignService: CampaignService,
                private translate: TranslateService,
                private router: Router,
    ) {
        translate.get('campaigns.confirm.deletion.prefix').subscribe((res: string) => {
            this.deletionConfirmationTextPrefix = res;
        });
        translate.get('campaigns.confirm.deletion.suffix').subscribe((res: string) => {
            this.deletionConfirmationTextSuffix = res;
        });
    }

    ngOnInit() {
        this.loadAll();
    }

    ngOnDestroy(): void {
        this.unsuscribeLastCampaignReport();
    }

    loadAll() {
        this.campaignService.findAllCampaigns().subscribe(
            (res) => this.campaigns = res,
            (error) => console.log(error)
        );

        this.findLastCampaignReports();
    }

    createCampaign() {
        const url = '/campaign/edition';
        this.router.navigateByUrl(url);
    }

    editCampaign(campaign: Campaign) {
        const url = '/campaign/' + campaign.id + '/edition';
        this.router.navigateByUrl(url);
    }

    deleteCampaign(id: number, title: string) {
        if (confirm(this.deletionConfirmationTextPrefix + title.toUpperCase() + this.deletionConfirmationTextSuffix)) {
            this.campaignService.delete(id).subscribe(
                () => {
                    this.campaigns.splice(this.getIndexFromId(id), 1);
                    this.campaigns = this.campaigns.slice();
                });
        }
    }

    private getIndexFromId(id: number): number {
        return this.campaigns.findIndex((campaign: Campaign) => campaign.id === id);
    }

    private findLastCampaignReports() {
        this.campaignService.findLastCampaignReports().subscribe(
            (lastCampaignReports) => {
                this.lastCampaignReports = lastCampaignReports;
                if (CampaignService.existRunningCampaignReport(lastCampaignReports)) {
                    this.unsuscribeLastCampaignReport();
                    this.lastCampaignReportsSub = timer(5000).subscribe(() => this.findLastCampaignReports());
                }
            },
            (error) => console.log(error)
        );
    }

    private unsuscribeLastCampaignReport() {
        if (this.lastCampaignReportsSub) this.lastCampaignReportsSub.unsubscribe();
    }
}
